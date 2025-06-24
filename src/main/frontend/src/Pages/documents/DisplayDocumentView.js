import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function DocuRoom() {
  const [searchTerm, setSearchTerm] = useState("");
  const [documents, setDocuments] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchAllDocuments();
  }, []);

  const fetchAllDocuments = async () => {
    try {
      const response = await axios.get("/api/documents");
      setDocuments(response.data);
    } catch (error) {
      console.error("문서 목록 조회 실패:", error);
    }
  };

  const handleSearch = async () => {
    try {
      // 검색어가 없으면 전체 목록 재조회
      if (!searchTerm.trim()) {
        fetchAllDocuments();
        return;
      }
      const response = await axios.get("/api/documents/search", {
        params: { title: searchTerm },
      });
      setDocuments(response.data);
    } catch (error) {
      console.error("검색 실패:", error);
    }
  };

  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  return (
    <div style={{ padding: "40px" }}>
      {/* 검색 바 */}
      <div style={{ marginBottom: "16px" }}>
        <input
          type="text"
          placeholder="문서 제목 검색"
          value={searchTerm}
          onChange={handleSearchChange}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()}
          style={{
            padding: "8px",
            fontSize: "16px",
            width: "300px",
            marginRight: "8px",
          }}
        />
        <button
          onClick={handleSearch}
          style={{ padding: "8px 16px", fontSize: "16px" }}
        >
          검색
        </button>
      </div>

      {/* 문서 테이블 */}
      <table
        style={{
          width: "100%",
          borderCollapse: "collapse",
          marginTop: "16px",
          fontSize: "16px",
        }}
      >
        <thead>
          <tr style={{ background: "#f5f5f5", borderBottom: "1px solid #ccc" }}>
            <th>번호</th>
            <th>문서 제목</th>
            <th>작성자</th>
            <th>날짜</th>
          </tr>
        </thead>
        <tbody style={{ textAlign: "center" }}>
          {documents.map((doc, index) => (
            <tr
              key={doc.docID}
              style={{ cursor: "pointer" }}
              onClick={() => navigate(`/documents/${doc.docID}`)}
            >
              <td>{index + 1}</td>
              <td>{doc.title}</td>
              <td>{doc.writerID}</td>
              <td>{formatDate(doc.date)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
