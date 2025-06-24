import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import './TaskCreateView.css';
import { useNavigate } from 'react-router-dom';
import { UserContext } from '../../Context/UserContext';
function TaskCreateView() {
    const nav = useNavigate();
    const {loggedInUser} = useContext(UserContext);


    const [formData, setFormData] = useState({
        title: '',
        startDate: '',
        endDate: '',
        visibility: 'private',
        notification: 'off',
        assignee: '',
        unitTask: '',
        publicStartDate: '',
        publicEndDate: '',
    });

    const [employees, setEmployees] = useState([]);
    const [selectedFile, setSelectedFile] = useState(null);

   useEffect(() => {
    const fetchEmployees = async () => {
        try {
            const response = await axios.get(`/api/tasks/open?deptId=${loggedInUser.deptId}`);
            const employeeList = response.data.employees; // 서버에서 "employees" 키로 전달된 리스트

            if (!Array.isArray(employeeList)) {
                throw new Error("직원 목록 형식이 올바르지 않습니다.");
            }

            setEmployees(employeeList);
        } catch (error) {
            console.error("담당자 목록 조회 실패:", error);
            alert("직원 정보를 불러오지 못했습니다.");
        }
    };

    fetchEmployees();
}, [loggedInUser.deptId]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleFileChange = (e) => {
        setSelectedFile(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const selectedEmployee = employees.find(emp => emp.employeeId === formData.assignee);
        const managerName = selectedEmployee ? selectedEmployee.name : '';

        const uploadData = new FormData();

        const taskDtoPayload = {
            title: formData.title,
            startDate: formData.startDate,
            endDate: formData.endDate,
            isPublic: formData.visibility === 'public',
            alarmEnabled: formData.notification === 'on',
            managerId: formData.assignee,
            managerName: managerName,
            deptId: loggedInUser.deptId, 
            unitTask: formData.unitTask,
            publicStartDate: formData.visibility === 'public' ? formData.publicStartDate : null,
            publicEndDate: formData.visibility === 'public' ? formData.publicEndDate : null,
            creatorId: loggedInUser.id,
        };

        uploadData.append('taskDto', new Blob([JSON.stringify(taskDtoPayload)], { type: "application/json" }));
        
        if (selectedFile) {
            uploadData.append('file', selectedFile);
        }

        try {
            const res = await axios.post("/api/tasks", uploadData);
            alert("✅ " + res.data); 
            nav('/task');
        } catch (err) {
            alert("❌ 등록 실패: " + (err.response?.data || "오류 발생"));
        }
    };

    return (
        <div className="task-create-container">
            <h1 className="task-create-header">업무 등록</h1>
            <form onSubmit={handleSubmit} className="task-create-form">
                <label>
                    제목:
                    <input type="text" name="title" value={formData.title} onChange={handleChange} required />
                </label>
                
                <label>
                    첨부 파일:
                    <input type="file" onChange={handleFileChange} />
                </label>

                <label>
                    시작일:
                    <input type="date" name="startDate" value={formData.startDate} onChange={handleChange} required />
                </label>

                <label>
                    종료일:
                    <input type="date" name="endDate" value={formData.endDate} onChange={handleChange} required />
                </label>

                <label>
                    공개 여부:
                    <select name="visibility" value={formData.visibility} onChange={handleChange}>
                        <option value="private">비공개</option>
                        <option value="public">공개</option>
                    </select>
                </label>

                {formData.visibility === 'public' && (
                    <>
                        <label>
                            공개 시작일:
                            <input type="date" name="publicStartDate" value={formData.publicStartDate} onChange={handleChange} />
                        </label>

                        <label>
                            공개 종료일:
                            <input type="date" name="publicEndDate" value={formData.publicEndDate} onChange={handleChange} />
                        </label>
                    </>
                )}

                <label>
                    알림 여부:
                    <select name="notification" value={formData.notification} onChange={handleChange}>
                        <option value="off">끄기</option>
                        <option value="on">켜기</option>
                    </select>
                </label>

                <label>
                    담당자:
                    <select name="assignee" value={formData.assignee} onChange={handleChange} required>
                        <option value="" disabled>-- 담당자 선택 --</option>
                        {employees.map(employee => (
                            <option key={employee.employeeId} value={employee.employeeId}>
                                {employee.name}
                            </option>
                        ))}
                    </select>
                </label>

                <label>
                    단위 업무:
                    <input type="text" name="unitTask" value={formData.unitTask} onChange={handleChange} />
                </label>

                <button type="submit" className="submit-button">등록</button>
            </form>
        </div>
    );
}

export default TaskCreateView;