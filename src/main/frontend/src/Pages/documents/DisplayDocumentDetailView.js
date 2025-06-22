import React, { useEffect, useState } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";

export default function DocumentDetailView() {
  const { id } = useParams(); // URL에서 id 추출
  const navigate = useNavigate();
  const [doc, setDoc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (id) {
      axios
        .get(`/api/documents/${id}`)
        .then((res) => {
          setDoc(res.data);
          setLoading(false);
          console.log("문서 조회 성공:", res.data);
        })
        .catch((err) => {
          console.error("문서 조회 실패:", err);
          setError("문서를 불러오는데 실패했습니다.");
          setLoading(false);
        });
    }
  }, [id]);

  if (loading) return <div style={{ padding: 40 }}>로딩 중...</div>;
  if (error) return <div style={{ padding: 40, color: "red" }}>{error}</div>;
  if (!doc) return <div style={{ padding: 40 }}>문서를 찾을 수 없습니다.</div>;

  return (
    <div style={{ padding: 40, maxWidth: 800, margin: "0 auto" }}>
      <button
        onClick={() => navigate(-1)}
        style={{
          marginBottom: 20,
          padding: "8px 16px",
          backgroundColor: "#007bff",
          color: "white",
          border: "none",
          borderRadius: "4px",
          cursor: "pointer",
        }}
      >
        ← 뒤로가기
      </button>

      <div
        style={{
          border: "1px solid #ddd",
          borderRadius: "8px",
          padding: "20px",
        }}
      >
        <h2 style={{ marginBottom: "16px", color: "#333" }}>{doc.title}</h2>

        <div style={{ marginBottom: "20px", fontSize: "14px", color: "#666" }}>
          <p style={{ margin: "4px 0" }}>
            <strong>작성자:</strong> {doc.writerID || "알 수 없음"}
          </p>
          <p style={{ margin: "4px 0" }}>
            <strong>날짜:</strong>{" "}
            {new Date(doc.date).toLocaleDateString("ko-KR")}
          </p>
          <p style={{ margin: "4px 0" }}>
            <strong>공개범위:</strong> {doc.visibility}
          </p>
        </div>

        <div
          style={{
            whiteSpace: "pre-wrap",
            lineHeight: "1.6",
            backgroundColor: "#f8f9fa",
            padding: "16px",
            borderRadius: "4px",
            minHeight: "200px",
          }}
        >
          {doc.content}
        </div>

        {doc.attachments && doc.attachments.length > 0 && (
          <div style={{ marginTop: 20 }}>
            <strong>첨부파일:</strong>
            <ul style={{ listStyle: "none", padding: 0, marginTop: 8 }}>
              {doc.attachments.map((att, idx) => (
                <li key={idx}>
                  <a
                    href={att.path}
                    download
                    style={{ color: "#007bff", textDecoration: "none" }}
                  >
                    {att.originalName}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}
