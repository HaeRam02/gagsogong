// src/pages/ClubMain.js (예상 경로)

import React, { useEffect, useState, useContext } from "react"; // useContext 추가
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import { UserContext } from "../../Context/UserContext"; // UserContext 경로 확인 및 임포트

const ClubMain = () => {
    const nav = useNavigate();
    const location = useLocation();

    const [clubs, setClubs] = useState([]);
    const [sort, setSort] = useState("popular");

    const { loggedInUser } = useContext(UserContext); // UserContext에서 loggedInUser 가져오기

    useEffect(() => {
        // loggedInUser가 없거나 ID가 없으면 API 호출하지 않음
        if (!loggedInUser || !loggedInUser.id) {
            console.warn("ClubMain: 로그인된 사용자 정보 (ID)가 없어 동호회 목록을 로드하지 않습니다.");
            setClubs([]); // 동호회 목록 비우기
            return;
        }

        // ⭐ username 대신 loggedInUser.id 사용
        const userId = loggedInUser.id;

        axios
            .get(`/api/clubs?sort=${sort}&userId=${userId}`)
            .then(res => setClubs(res.data))
            .catch(err => {
                console.error("동호회 조회 실패:", err);
                // 에러 발생 시 사용자에게 알림 또는 적절한 에러 메시지 표시
                alert("동호회 목록을 불러오는데 실패했습니다. 잠시 후 다시 시도해주세요.");
            });
    }, [sort, location.key, loggedInUser]); // ⭐ loggedInUser를 의존성 배열에 추가

    const renderTable = () => (
        <div>
            <table
                border="1"
                cellPadding="10"
                style={{
                    width: "100%",
                    borderCollapse: "collapse",
                    alignItems: "center"
                }}
            >
                <thead>
                <tr>
                    <th style={{ textAlign: "center" }}>#</th>
                    <th style={{ textAlign: "center" }}>동호회명</th>
                    <th>동호회 설명</th>
                    <th style={{ textAlign: "center" }}>생성일</th>
                    <th style={{ textAlign: "center" }}>회원 수</th>
                </tr>
                </thead>
                <tbody>
                {clubs.length > 0 ? ( // 동호회 데이터가 있을 때만 렌더링
                    clubs.map((club, index) => (
                        <tr key={club.clubId}>
                            <td style={{ textAlign: "center" }}>{index + 1}</td>
                            <td style={{ textAlign: "center" }}>{club.name}</td>
                            <td>{club.description}</td>
                            <td style={{ textAlign: "center" }}>
                                {club.createDate?.split("T")[0]}
                            </td>
                            <td style={{ textAlign: "center" }}>{club.memberCount}</td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="5" style={{ textAlign: "center", padding: "20px" }}>
                            {loggedInUser ? "등록된 동호회가 없습니다." : "로그인이 필요합니다."}
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );

    return (
        <div style={{ padding: "50px", fontFamily: "Arial" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h1>동호회</h1>
                <button
                    onClick={() => {
                        // ⭐⭐⭐ 이 부분이 바로 관리자 권한 확인 로직입니다! ⭐⭐⭐
                        if (!loggedInUser || loggedInUser.role !== 'ADMIN') {
                            alert("관리자만 동호회를 등록할 수 있습니다.");
                            return; // 함수 실행 중단
                        }
                        nav("/club/register"); // ADMIN 권한이 있는 경우에만 페이지 이동
                    }}
                    style={{
                        padding: "10px 20px",
                        fontSize: "16px",
                        backgroundColor: "#f0f0f0",
                        border: "1px solid #ccc",
                        cursor: "pointer"
                    }}
                >
                    동호회 등록
                </button>
            </div>

            <div style={{ margin: "20px 0" }}>
                <button onClick={() => setSort("popular")} style={{ marginRight: "10px" }}>
                    인기순
                </button>
                <button onClick={() => setSort("newest")}>신규순</button>
            </div>

            {renderTable()}
        </div>
    );
};

export default ClubMain;