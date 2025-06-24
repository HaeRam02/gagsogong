// src/pages/TaskRoom.js (예상 경로)

import React, { useState, useContext } from "react"; // useContext 임포트 추가
import { useNavigate } from "react-router-dom";
import { UserContext } from "../../Context/UserContext"; // UserContext 경로 확인 및 임포트
import DisplayDocumentView from "./DisplayDocumentView"; // 이 컴포넌트 이름이 맞는지 확인해주세요. TaskRoom에서 직접 렌더링하는 컴포넌트의 이름이 `RegisterDocumentView`가 아니라 `DisplayDocumentView`로 되어 있습니다. 만약 문서 등록 폼이 `RegisterDocumentView`라면 아래 import 경로를 수정해야 합니다.

export default function TaskRoom() {
    const [search, setSearch] = useState("");
    const navigate = useNavigate();
    const { loggedInUser } = useContext(UserContext); // UserContext에서 loggedInUser 가져오기

    const goToCreate = () => {
        // ⭐ 관리자 권한 확인 로직 추가 ⭐
        if (!loggedInUser || loggedInUser.role !== 'ADMIN') {
            alert("❌ 관리자만 문서를 등록할 수 있습니다.");
            return; // 페이지 이동 중단
        }
        navigate("/document/register");
    };

    return (
        <div style={{ padding: "20px" }}>
            <h1 style={{ marginLeft: "20px" }}>문서방</h1>

            <h2 style={{ marginLeft: "20px" }}>문서 등록 하기</h2>
            <button onClick={goToCreate} className="addBtn">
                문서 등록
            </button>
            <DisplayDocumentView/>
        </div>
    );
}