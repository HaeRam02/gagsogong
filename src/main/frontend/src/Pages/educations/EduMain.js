import React, { useState, useEffect, useContext } from "react";
import styles from "./EduMain.module.css";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { UserContext } from "../../Context/UserContext";

const EduMain = () => {
  const [showForm, setShowForm] = useState(false);
  const [educations, setEducations] = useState([]);
  const [filteredEducations, setFilteredEducations] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const {loggedInUser} = useContext(UserContext);

  const itemsPerPage = 5;
  const navigate = useNavigate();

  const handlePageChange = (page) => {
    if (page >= 1 && page <= Math.ceil(filteredEducations.length / itemsPerPage)) {
      setCurrentPage(page);
    }
  };




  const [form, setForm] = useState({
    title: "",
    instructor: "",
    educationType: "온라인",
    applicationPeriodStart: "",
    applicationPeriodEnd: "",
    educationPeriodStart: "",
    educationPeriodEnd: "",
    attachmentType: "파일",
    attachmentPath: "",
    attachmentFile: null,
  });

  const [searchKeyword, setSearchKeyword] = useState("");
  const [searchEduType, setSearchEduType] = useState("전체");
  const [searchStatus, setSearchStatus] = useState("전체");

  useEffect(() => {
    axios.get("/api/educations")
      .then((res) => {
        setEducations(res.data);
        setFilteredEducations(res.data);
      })
      .catch((err) => console.error("목록 로딩 오류", err));
  }, []);

  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (name === "attachmentPath" && form.attachmentType === "파일") {
      setForm({ ...form, attachmentPath: files[0]?.name || "", attachmentFile: files[0] });
    } else {
      setForm({ ...form, [name]: value });
    }
  };

 const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    const education = {
      title: form.title,
      instructor: form.instructor,
      educationType: form.educationType,
      applicationPeriodStart: form.applicationPeriodStart,
      applicationPeriodEnd: form.applicationPeriodEnd,
      educationPeriodStart: form.educationPeriodStart,
      educationPeriodEnd: form.educationPeriodEnd,
      attachmentType: form.attachmentType,
      attachmentPath: form.attachmentPath,
    };

    formData.append("education", new Blob([JSON.stringify(education)], { type: "application/json" }));
    if (form.attachmentType === "파일" && form.attachmentFile) {
      formData.append("file", form.attachmentFile);
    }

    try {
      // ⭐ 백엔드 응답을 'res' 변수에 저장합니다.
      const res = await axios.post("/api/educations", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      alert("교육정보 등록 완료");
      setShowForm(false);

      // ⭐ 백엔드에서 반환된 Education 객체 (res.data)를 사용하여 상태를 업데이트합니다.
      setEducations(prevEducations => [...prevEducations, res.data]);
      setFilteredEducations(prevFilteredEducations => [...prevFilteredEducations, res.data]);
      setCurrentPage(1); // 새로 추가된 항목이 첫 페이지에 보이도록
      setForm({ // 폼 초기화
          title: "",
          instructor: "",
          educationType: "온라인",
          applicationPeriodStart: "",
          applicationPeriodEnd: "",
          educationPeriodStart: "",
          educationPeriodEnd: "",
          attachmentType: "파일",
          attachmentPath: "",
          attachmentFile: null,
      });

    } catch (err) {
      console.error("등록 오류", err.response ? err.response.data : err.message); // 상세 에러 로깅
      alert("등록 실패: " + (err.response ? err.response.data.message || err.response.statusText : err.message));
    }
  };

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

  const handleSearch = () => {
    const filtered = educations.filter((edu) => {
      const matchTitle = edu.title.includes(searchKeyword);
      const matchEduType = searchEduType === "전체" || edu.educationType === searchEduType;
      const status = getRecruitStatus(edu.applicationPeriodStart, edu.applicationPeriodEnd);
      const matchStatus =
        searchStatus === "전체" ||
        (searchStatus === "모집중" && status.includes("모집 중")) ||
        (searchStatus === "모집예정" && status === "모집 예정") ||
        (searchStatus === "마감" && status === "마감");
      return matchTitle && matchEduType && matchStatus;
    });
    setFilteredEducations(filtered);
    setCurrentPage(1);
  };

  const totalPages = Math.ceil(filteredEducations.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedEducations = filteredEducations.slice(startIndex, startIndex + itemsPerPage);

  if (!showForm) {
    return (
      <div className={styles.container}>
        <div className={styles.wrapper}>
          <div className={styles.submitRowRight}>
            <button className={styles.submitBtn} onClick={() =>{
            if(loggedInUser.role !== 'ADMIN'){
              alert("권환이 없습니다");
              return;
            } 
              setShowForm(true);
          }}
              >교육정보 등록</button>
              
          </div>
          <h2 className={styles.title}>교육 목록 조회</h2>
          <div className={styles.formRow}>
            <label>교육명</label>
            <input type="text" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} />
          </div>

          <div className={styles.formRowGroup}>
            <div className={styles.shortFormRow}>
              <label>교육형태:</label>
              <div className={styles.inline}>
                {['전체', '온라인', '오프라인'].map((type) => (
                  <label key={type}>
                    <input
                      type="radio"
                      name="searchEduType"
                      value={type}
                      checked={searchEduType === type}
                      onChange={(e) => setSearchEduType(e.target.value)}
                    />
                    {type}
                  </label>
                ))}
              </div>
            </div>
            <div className={styles.shortFormRow}>
              <label>상태:</label>
              <div className={styles.inline}>
                {['전체', '모집중', '모집예정', '마감'].map((status) => (
                  <label key={status}>
                    <input
                      type="radio"
                      name="searchStatus"
                      value={status}
                      checked={searchStatus === status}
                      onChange={(e) => setSearchStatus(e.target.value)}
                    />
                    {status}
                  </label>
                ))}
              </div>
            </div>
          </div>

          <div className={styles.submitRowRight}>
            <button className={styles.submitBtn} onClick={handleSearch}>조회</button>
          </div>

          <h3 style={{ marginTop: "40px" }}>교육 목록</h3>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>교육 명</th>
                <th>교육 형태</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              {paginatedEducations.length > 0 ? (
                paginatedEducations.map((edu) => (
                  <tr key={edu.educationId} onClick={() => navigate(`/education/${edu.educationId}`)} style={{ cursor: "pointer" }}>
                    <td>{edu.title}</td>
                    <td>{edu.educationType}</td>
                    <td>{getRecruitStatus(edu.applicationPeriodStart, edu.applicationPeriodEnd)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3} style={{ textAlign: "center", padding: "20px" }}>검색 결과가 없습니다.</td>
                </tr>
              )}
            </tbody>
          </table>

          <div className={styles.paginationRow} style={{ textAlign: "center", marginTop: "16px" }}>
            <span style={{ margin: "0 4px", cursor: "pointer" }} onClick={() => handlePageChange(currentPage - 1)}>{"<이전"}</span>
            {Array.from({ length: totalPages }, (_, i) => (
              <span key={i + 1} style={{ margin: "0 4px", fontWeight: currentPage === i + 1 ? "bold" : "normal", cursor: "pointer" }} onClick={() => handlePageChange(i + 1)}>{i + 1}</span>
            ))}
            <span style={{ margin: "0 4px", cursor: "pointer" }} onClick={() => handlePageChange(currentPage + 1)}>{"다음>"}</span>
          </div>
        </div>
      </div>
    );
  }

  // 등록 화면
  return (
    <div className={styles.container}>
      <div className={styles.wrapper}>
        <h2 className={styles.title}>교육정보 등록</h2>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.formRow}>
            <label>교육명:</label>
            <input type="text" name="title" value={form.title} onChange={handleChange} required maxLength={200} />
          </div>

          <div className={styles.formRowGroup}>
            <div className={styles.shortFormRow}>
              <label>담당자명:</label>
              <input type="text" name="instructor" value={form.instructor} onChange={handleChange} required maxLength={100} />
            </div>
            <div className={styles.shortFormRow}>
              <label>교육형태:</label>
              <div className={styles.inline}>
                <label><input type="radio" name="educationType" value="온라인" checked={form.educationType === "온라인"} onChange={handleChange} /> 온라인</label>
                <label><input type="radio" name="educationType" value="오프라인" checked={form.educationType === "오프라인"} onChange={handleChange} /> 오프라인</label>
              </div>
            </div>
          </div>

          <div className={styles.formRowGroup}>
            <div className={styles.shortFormRow}><label>신청 시작일:</label><input type="date" name="applicationPeriodStart" value={form.applicationPeriodStart} onChange={handleChange} required /></div>
            <div className={styles.shortFormRow}><label>신청 종료일:</label><input type="date" name="applicationPeriodEnd" value={form.applicationPeriodEnd} onChange={handleChange} required /></div>
          </div>

          <div className={styles.formRowGroup}>
            <div className={styles.shortFormRow}><label>교육 시작일:</label><input type="date" name="educationPeriodStart" value={form.educationPeriodStart} onChange={handleChange} required /></div>
            <div className={styles.shortFormRow}><label>교육 종료일:</label><input type="date" name="educationPeriodEnd" value={form.educationPeriodEnd} onChange={handleChange} required /></div>
          </div>

          {form.educationType === "온라인" && (
            <>
              <div className={styles.formRow}><label>첨부파일 종류:</label>
                <div className={styles.inline}>
                  <label><input type="radio" name="attachmentType" value="파일" checked={form.attachmentType === "파일"} onChange={handleChange} /> 파일</label>
                  <label><input type="radio" name="attachmentType" value="URL" checked={form.attachmentType === "URL"} onChange={handleChange} /> URL</label>
                </div>
              </div>

              {form.attachmentType === "파일" ? (
                <div className={styles.formRow}><label>파일 업로드:</label>
                  <input type="file" name="attachmentPath" onChange={handleChange} />
                </div>
              ) : (
                <div className={styles.formRow}><label>첨부파일 경로:</label>
                  <input type="text" name="attachmentPath" value={form.attachmentPath} onChange={handleChange} maxLength={500} />
                </div>
              )}
            </>
          )}

          <div className={styles.submitRowRight}>
            <button type="submit" className={styles.submitBtn}>교육정보 등록</button>
            <button type="button" className={styles.submitBtn} style={{ marginLeft: "12px" }} onClick={() => setShowForm(false)}>등록 취소</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EduMain;
