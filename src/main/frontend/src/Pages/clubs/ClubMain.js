import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";

const ClubMain = () => {
    const nav = useNavigate();
    const location = useLocation();

    const [clubs, setClubs] = useState([]);
    const [sort, setSort] = useState("popular");

    useEffect(() => {
    const username = localStorage.getItem("username");  // ✅ 이 변수명 사용
    axios
        .get(`/api/clubs?sort=${sort}&userId=${username}`) // ✅ 맞는 이름으로 호출
        .then(res => setClubs(res.data))
        .catch(err => console.error("조회 실패:", err));
}, [sort, location.key]);

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
                    {clubs.map((club, index) => (
                        <tr key={club.clubId}>
                            <td style={{ textAlign: "center" }}>{index + 1}</td>
                            <td style={{ textAlign: "center" }}>{club.name}</td>
                            <td>{club.description}</td>
                            <td style={{ textAlign: "center" }}>
                                {club.createDate?.split("T")[0]}
                            </td>
                            <td style={{ textAlign: "center" }}>{club.memberCount}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );

    return (
        <div style={{ padding: "50px", fontFamily: "Arial" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h1>동호회</h1>
                <button
                    onClick={() => nav("/club/register")}
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
