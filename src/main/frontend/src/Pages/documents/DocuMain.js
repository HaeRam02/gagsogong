import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import RegisterDocumentView from "./DisplayDocumentView";

export default function TaskRoom() {
  const [search, setSearch] = useState("");
  const navigate = useNavigate();

  const goToCreate = () => {
    navigate("/document/read");
  };
  return (
    <div style={{ padding: "20px" }}>
      <h1 style={{ marginLeft: "20px" }}>문서방</h1>

      <h2 style={{ marginLeft: "20px" }}>문서 등록 하기</h2>
      <button onClick={goToCreate} className="addBtn">
        문서 등록
      </button>

      <RegisterDocumentView />
    </div>
  );
}
