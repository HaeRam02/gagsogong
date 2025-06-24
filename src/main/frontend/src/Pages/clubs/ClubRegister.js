import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const ClubRegister = () => {
    const [form, setForm] = useState({
        name: "",
        description: "",
        creatorName: "",
        createDate: "",
        visibility: "PUBLIC",
        memberCount: 0,
    });

    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.post("/api/clubs/register", form);
            alert("동호회 등록 완료!");
            navigate("/club");
        } catch (err) {
            alert("등록 실패: " + (err.response?.data?.message || err.message));
        }
    };

    return (
        <div style={{ maxWidth: "1000px", margin: "40px auto", fontFamily: "Arial" }}>
            <h2 style={{ marginBottom: "20px" }}>동호회 등록</h2>
            <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "15px" }}>
                <label>
                    <div style={{ marginBottom: "5px", fontWeight: "bold" }}>등록자 이름</div>
                    <input
                        type="text"
                        name="creatorName"
                        value={form.creatorName}
                        onChange={handleChange}
                        required
                        style={{ width: "100%", padding: "8px" }}
                    />
                </label>

                <label>
                    <div style={{ marginBottom: "5px", fontWeight: "bold" }}>동호회명</div>
                    <input
                        type="text"
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        required
                        style={{ width: "100%", padding: "8px" }}
                    />
                </label>

                <label>
                    <div style={{ marginBottom: "5px", fontWeight: "bold" }}>내용</div>
                    <textarea
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        rows="5"
                        style={{ width: "100%", resize: "vertical", padding: "8px" }}
                    />
                </label>

                <label>
                    <div style={{ marginBottom: "5px", fontWeight: "bold" }}>동호회 생성 날짜</div>
                    <input
                        type="datetime-local"
                        name="createDate"
                        value={form.createDate}
                        onChange={handleChange}
                        required
                        style={{ width: "100%", padding: "8px" }}
                    />
                </label>

                <label>
                    <div style={{ marginBottom: "5px", fontWeight: "bold" }}>공개범위</div>
                    <select
                        name="visibility"
                        value={form.visibility}
                        onChange={handleChange}
                        style={{ width: "100%", padding: "8px" }}
                    >
                        <option value="PUBLIC">공개</option>
                        <option value="GROUP">그룹공개</option>
                        <option value="PRIVATE">비공개</option>
                    </select>
                </label>

                <label>
                    <div style={{ marginBottom: "5px", fontWeight: "bold" }}>멤버 수</div>
                    <input
                        type="number"
                        name="memberCount"
                        value={form.memberCount}
                        onChange={handleChange}
                        min="0"
                        required
                        style={{ width: "100%", padding: "8px" }}
                    />
                </label>

                <button
                    type="submit"
                    style={{
                        padding: "10px",
                        backgroundColor: "#ddd",
                        border: "none",
                        cursor: "pointer",
                        width: "150px",
                        alignSelf: "flex-end",
                    }}
                >
                    동호회 등록
                </button>
            </form>
        </div>
    );
};

export default ClubRegister;