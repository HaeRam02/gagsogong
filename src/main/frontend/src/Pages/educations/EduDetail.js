import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "./EduMain.module.css"; // 스타일 재활용

const EduDetail = () => {
  const { id } = useParams(); // educationId (UUID)
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get(`/api/educations/${id}`)
      .then((res) => {
        setData(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("교육 정보 불러오기 실패", err);
        setLoading(false);
      });
  }, [id]);

  const getRecruitStatus = (startStr, endStr) => {
    const today = new Date();
    const startDate = new Date(startStr);
    const endDate = new Date(endStr);

    today.setHours(0, 0, 0, 0);
    startDate.setHours(0, 0, 0, 0);
    endDate.setHours(0, 0, 0, 0);

    if (today < startDate) {
      return "모집 예정";
    } else if (today >= startDate && today <= endDate) {
      const diffDays = Math.ceil((endDate - today) / (1000 * 60 * 60 * 24));
      return `모집 중 (D-${diffDays})`;
    } else {
      return "마감";
    }
  };


  if (loading) return <div>불러오는 중...</div>;
  if (!data) return <div>존재하지 않는 교육입니다.</div>;

  const recruitStatus = getRecruitStatus(data.applicationPeriodStart, data.applicationPeriodEnd);
  const isRecruiting = recruitStatus.startsWith("모집 중");


  return (

    <div className={styles.container}>
      <div className={styles.wrapper}>


        <h2 className={styles.title}>교육정보 상세 조회</h2>

        <div style={{
          border: "1px solid #ccc",
          borderRadius: "8px",
          padding: "24px",
          backgroundColor: "#fafafa",
          lineHeight: 1.8
        }}>
          <p><strong>{data.title}</strong></p>
          <p>교육 형태: {data.educationType}</p>
          <p>신청 기간: {data.applicationPeriodStart} ~ {data.applicationPeriodEnd}</p>
          <p>교육 기간: {data.educationPeriodStart} ~ {data.educationPeriodEnd}</p>
          <p>담당자: {data.instructor}</p>
          <p>상태: {recruitStatus}</p>


          {/* 구분선 + 여백 */}
          <hr style={{ margin: "24px 0", borderColor: "#ddd" }} />

          <p><strong>[첨부파일]</strong></p>
          <ul>
            {data.attachmentType === "파일" ? (
              <li>
                <a href={`http://localhost:8080/api/educations/file/${data.attachmentPath}`} download>
                  {data.attachmentPath} (다운로드)
                </a>
              </li>

            ) : (
              <li><a href={data.attachmentPath} target="_blank" rel="noreferrer">{data.attachmentPath} (URL 링크)</a></li>
            )}
          </ul>
        </div>

        <div style={{ margin: "30px 0" }} />
        <div className={styles.submitRowRight} style={{ marginTop: "16px", display: "flex", gap: "12px" }}>
          {isRecruiting && (
            <button className={styles.submitBtn}>신청하기</button>
          )}
          <button className={styles.submitBtn} onClick={() => navigate(-1)}>닫기</button>
        </div>

      </div>
    </div>
  );
};

export default EduDetail;
