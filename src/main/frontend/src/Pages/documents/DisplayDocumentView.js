import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function DocuRoom() {
  const [search, setSearch] = useState("");
  const [documents, setDocuments] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
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

  // const handleSearch = async () => {
  //     try {
  //         const response = await axios.get('/api/documents/search', {
  //             params: { title: searchTerm },
  //         });
  //         setTasks(response.data);
  //     } catch (error) {
  //         console.error('검색 실패:', error);
  //     }
  // };

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
      {/* 테이블 */}
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
            <th>업무 제목</th>
            <th>담당자</th>
            <th>날짜</th>
            {/* <th>종료일</th> */}
          </tr>
        </thead>
        <tbody style={{ textAlign: "center" }}>
          {documents.map((document, index) => (
            <tr
              key={document.docID}
              style={{ cursor: "pointer" }}
              onClick={() => navigate(`/documents/${document.docID}`)} // ← 클릭 시 이동
            >
              <td>{index + 1}</td>
              <td>{document.title}</td>
              <td>{document.writerID}</td>
              <td>{formatDate(document.date)}</td>
              {/* <td>{}</td> */}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
