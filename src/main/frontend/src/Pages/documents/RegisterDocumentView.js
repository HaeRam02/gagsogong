// src/pages/RegisterDocumentView.js (혹은 해당 경로)

import React, { useState, useEffect, useContext } from "react"; // useContext 임포트
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { UserContext } from "../../Context/UserContext"; // UserContext 경로 확인 및 임포트

export default function RegisterDocumentView({ onRegister }) {
    const nav = useNavigate();
    const { loggedInUser } = useContext(UserContext); // UserContext에서 loggedInUser 가져오기

    const [files, setFiles] = useState([]);
    const [formData, setFormData] = useState({
        docID: "",
        writerID: "",
        content: "",
        title: "",
        visibility: "",
        date: "",
    });

    // 컴포넌트 마운트 시 writerID를 loggedInUser.id로 설정
    useEffect(() => {
        if (loggedInUser && loggedInUser.id) {
            setFormData(prev => ({ ...prev, writerID: loggedInUser.id }));
        }
    }, [loggedInUser]); // loggedInUser가 변경될 때마다 실행

    const handleFileChange = (e) => {
        setFiles(Array.from(e.target.files));
    };

    const handleRegister = async (e) => {
        e.preventDefault();

        // ⭐ 관리자 권한 확인 로직 추가 ⭐
        if (!loggedInUser || loggedInUser.role !== 'ADMIN') {
            alert("❌ 관리자만 문서를 등록할 수 있습니다.");
            return; // 등록 기능 중단
        }

        const uploadData = new FormData();

        const documentDtoPayload = {
            writerID: loggedInUser.id, // ⭐ 로그인된 사용자의 ID 사용 ⭐
            title: formData.title,
            content: formData.content,
            visibility: formData.visibility === "public", // 불린 변환
        };

        uploadData.append(
            "documentDTO",
            new Blob([JSON.stringify(documentDtoPayload)], {
                type: "application/json",
            })
        );
        console.log("업로드 데이터:", documentDtoPayload);

        // 파일이 있을 때만 추가
        if (files && files.length > 0) {
            console.log("첨부된 파일들:", files);
            files.forEach((file) => {
                uploadData.append("files", file); // 각 파일을 같은 이름으로 추가
            });
        }

        try {
            const res = await axios.post("/api/documents", uploadData, {});
            alert("✅ " + res.data);
            nav("/document"); // 성공 시 문서 목록 페이지로 이동
        } catch (err) {
            console.error("전체 에러 객체:", err);
            console.error("에러 응답:", err.response);
            console.error("에러 요청:", err.request);

            let message = "등록에 실패했습니다.";

            if (err.response) {
                console.log("응답 상태:", err.response.status);
                console.log("응답 데이터:", err.response.data);

                if (typeof err.response.data === "string") {
                    message = err.response.data;
                } else if (err.response.data && err.response.data.message) {
                    message = err.response.data.message;
                } else {
                    message = `서버 오류: ${err.response.status}`;
                }
            } else if (err.request) {
                message = "서버에 연결할 수 없습니다.";
            } else {
                message = err.message || "알 수 없는 오류가 발생했습니다.";
            }

            alert("❌ 등록 실패: " + message);
        }
    };

    return (
        <form
            onSubmit={handleRegister}
            style={{
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
                    공개 범위 (public/private)
                </label>
                <input
                    type="text"
                    value={formData.visibility}
                    onChange={(e) =>
                        setFormData({
                            ...formData,
                            visibility: e.target.value,
                        })
                    }
                    placeholder="public 또는 private으로 입력해주세요."
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