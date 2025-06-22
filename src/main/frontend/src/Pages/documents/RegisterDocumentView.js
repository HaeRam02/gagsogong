import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function RegisterDocumentView({ onRegister }) {
  const nav = useNavigate();

  // const [title, setTitle] = useState("");
  // const [content, setContent] = useState("");
  // const [scope, setScope] = useState("");
  const [files, setFiles] = useState([]);
  // '로그인 되어 있다'고 가정한 가상의 사용자 정보
  const loggedInUser = {
    id: "EMP_007", // 로그인한 사용자의 ID
    name: "이순신", // 로그인한 사용자의 이름
    deptId: "D101", // 로그인한 사용자의 부서 ID
  };

  const [formData, setFormData] = useState({
    docID: "",
    writerID: "", //입력
    content: "", //입력
    title: "", //입력
    visibility: "", //입력
    date: "",
  });
  const [employees, setEmployees] = useState([]);

  // useEffect(() => {
  //   const fetchEmployees = async () => {
  //     // =================================================================
  //     // TODO: 추후 EmployeeProvider API가 구현되면 아래 코드로 교체
  //     // try {
  //     //     // 로그인한 사용자의 부서 ID를 사용해 해당 부서의 직원만 조회
  //     //     const response = await axios.get(`/api/employees?deptId=${loggedInUser.deptId}`);
  //     //     setEmployees(response.data);
  //     // } catch (error) {
  //     //     console.error("담당자 목록 조회 실패:", error);
  //     // }
  //     // =================================================================

  //     // 현재는 구현 전이므로, 가상의 데이터로 설정
  //     const dummyEmployees = [
  //       { employeeId: "EMP_001", name: "김철수" },
  //       { employeeId: "EMP_002", name: "이영희" },
  //       { employeeId: "EMP_003", name: "박민준" },
  //     ];
  //     setEmployees(dummyEmployees);
  //   };

  //   fetchEmployees();
  // }, []); // 나중에 실제 API를 사용하게 되면 [loggedInUser.deptId]로 변경하여 부서 ID가 바뀔 때마다 새로고침되게 할 수 있습니다.

  const handleFileChange = (e) => {
    setFiles(Array.from(e.target.files));
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    const uploadData = new FormData();
    const documentDtoPayload = {
      // docID: formData.docID,
      writerID: loggedInUser.id,
      title: formData.title,
      content: formData.content,
      visibility: formData.visibility === "public", // 불린 변환
      // date: formData.date,
    };
    uploadData.append(
      "documentDTO",
      new Blob([JSON.stringify(documentDtoPayload)], {
        type: "application/json",
      })
    );
    console.log("업로드 데이터:", documentDtoPayload);
    if (files) {
      uploadData.append("file", files);
    }
    try {
      const res = await axios.post("/api/documents", uploadData);
      alert("✅ " + res.data);
      nav("/document");
    } catch (err) {
      const message =
        typeof err.response.data === "string"
          ? err.response.data
          : err.response.data.message || JSON.stringify(err.response.data);
      alert("❌ 등록 실패: " + message);
    }
  };

  return (
    <form
      onSubmit={handleRegister}
      style={{
        // 가운데 정렬 없앰
        margin: "40px 0",
        padding: "32px 32px 24px 32px",
        borderRadius: 8,
        background: "#fff",
        width: "100%",
        maxWidth: "1100px",
        minWidth: "600px",
        boxSizing: "border-box",
      }}
    >
      <div style={{ marginBottom: 20 }}>
        <label style={{ display: "block", marginBottom: 6, fontWeight: 500 }}>
          문서 제목
        </label>
        <input
          type="text"
          value={formData.title}
          // onChange={(e) => setTitle(e.target.value)}
          onChange={(e) =>
            setFormData({
              ...formData,
              title: e.target.value,
            })
          }
          style={{
            width: "100%",
            height: 36,
            fontSize: 16,
            boxSizing: "border-box",
          }}
        />
      </div>

      <div style={{ marginBottom: 20 }}>
        <label style={{ display: "block", marginBottom: 6, fontWeight: 500 }}>
          내용
        </label>
        <textarea
          value={formData.content}
          // onChange={(e) => setContent(e.target.value)}
          onChange={(e) =>
            setFormData({
              ...formData,
              content: e.target.value,
            })
          }
          style={{
            width: "100%",
            height: 120,
            fontSize: 16,
            boxSizing: "border-box",
          }}
        />
      </div>

      <div style={{ marginBottom: 20 }}>
        <label style={{ display: "block", marginBottom: 6, fontWeight: 500 }}>
          공개 범위
        </label>
        <input
          type="text"
          value={formData.visibility}
          // onChange={(e) => setScope(e.target.value)}
          onChange={(e) =>
            setFormData({
              ...formData,
              visibility: e.target.value,
            })
          }
          style={{
            width: "100%",
            height: 36,
            fontSize: 16,
            boxSizing: "border-box",
          }}
        />
      </div>

      <div style={{ marginBottom: 20, display: "flex", alignItems: "center" }}>
        <label
          style={{ marginRight: 16, fontWeight: 500, whiteSpace: "nowrap" }}
        >
          첨부파일 등록
        </label>
        <input
          id="file-upload"
          type="file"
          multiple
          style={{ display: "none" }}
          onChange={handleFileChange}
        />
        <label
          htmlFor="file-upload"
          style={{
            display: "inline-block",
            border: "1px solid #333",
            padding: "6px 16px",
            background: "#fafafa",
            cursor: "pointer",
            borderRadius: 3,
            fontSize: 15,
          }}
        >
          첨부파일 추가
        </label>
        {files.length > 0 && (
          <div style={{ marginLeft: 16, fontSize: 13, color: "#666" }}>
            {files.map((file, idx) => (
              <span key={idx} style={{ marginRight: 10 }}>
                {file.name}
              </span>
            ))}
          </div>
        )}
      </div>

      <div style={{ textAlign: "right" }}>
        <button
          type="submit"
          style={{
            background: "#296177",
            color: "#fff",
            border: "none",
            borderRadius: 4,
            padding: "10px 36px",
            fontSize: 18,
            fontWeight: 500,
            cursor: "pointer",
          }}
        >
          등록
        </button>
      </div>
    </form>
  );
}
