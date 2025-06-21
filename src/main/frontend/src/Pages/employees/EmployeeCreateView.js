import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './EmployeeCreateView.css';
import { useNavigate } from 'react-router-dom';

function EmployeeCreateView() {
    const nav = useNavigate();

    const [formData, setFormData] = useState({
        employeeId: '',
        password: '',
        name: '',
        depId: '',
        depName: '',
        phoneNum: ''
    });

    // 부서 목록 데이터
    const [departments, setDepartments] = useState([
        { id: 'DEV001', name: '개발팀' },
        { id: 'DEV002', name: '프론트엔드팀' },
        { id: 'DEV003', name: '백엔드팀' },
        { id: 'DEV004', name: 'DevOps팀' },
        { id: 'MKT001', name: '마케팅팀' },
        { id: 'MKT002', name: '디지털마케팅팀' },
        { id: 'HR001', name: '인사팀' },
        { id: 'HR002', name: '채용팀' },
        { id: 'FIN001', name: '재무팀' },
        { id: 'FIN002', name: '회계팀' },
        { id: 'OPS001', name: '운영팀' },
        { id: 'OPS002', name: '고객지원팀' },
        { id: 'SALE001', name: '영업팀' },
        { id: 'SALE002', name: '영업지원팀' },
        { id: 'MGMT001', name: '경영진' }
    ]);

    useEffect(() => {
        // TODO: 추후 API에서 부서 목록을 가져올 때 사용
        // const fetchDepartments = async () => {
        //     try {
        //         const response = await axios.get('/api/departments');
        //         setDepartments(response.data);
        //     } catch (error) {
        //         console.error("부서 목록 조회 실패:", error);
        //     }
        // };
        // fetchDepartments();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleDepartmentChange = (e) => {
        const selectedDeptId = e.target.value;
        const selectedDept = departments.find(dept => dept.id === selectedDeptId);
        
        setFormData({
            ...formData,
            depId: selectedDeptId,
            depName: selectedDept ? selectedDept.name : ''
        });
    };

    const handlePhoneChange = (e) => {
        let value = e.target.value.replace(/[^0-9]/g, '');
        
        // 전화번호 형식 자동 적용 (010-1234-5678)
        if (value.length <= 3) {
            value = value;
        } else if (value.length <= 7) {
            value = value.replace(/(\d{3})(\d{0,4})/, '$1-$2');
        } else {
            value = value.replace(/(\d{3})(\d{4})(\d{0,4})/, '$1-$2-$3');
        }

        // 최대 길이 제한
        if (value.length > 13) {
            value = value.substring(0, 13);
        }

        setFormData({ ...formData, phoneNum: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.post("/api/employees", formData);
            alert(" 직원 등록 성공!");
            nav('/employee');
        } catch (error) {
            console.error('직원 등록 실패:', error);
            if (error.response && error.response.data && error.response.data.errors) {
                const errorMessages = error.response.data.errors.map(err => err.message).join('\n');
                alert(" 등록 실패:\n" + errorMessages);
            } else {
                alert(" 등록 실패: " + (error.response?.data || "오류 발생"));
            }
        }
    };

    return (
        <div className="employee-create-container">
            <h1 className="employee-create-header">직원 등록</h1>
            <form onSubmit={handleSubmit} className="employee-create-form">
                <label>
                    직원 ID:
                    <input 
                        type="text" 
                        name="employeeId" 
                        value={formData.employeeId} 
                        onChange={handleChange} 
                        placeholder="직원 ID를 입력하세요 (최대 20자)"
                        maxLength={20}
                        required 
                    />
                </label>

                <label>
                    비밀번호:
                    <input 
                        type="password" 
                        name="password" 
                        value={formData.password} 
                        onChange={handleChange} 
                        placeholder="비밀번호를 입력하세요 (최대 20자)"
                        maxLength={20}
                        required 
                    />
                </label>

                <label>
                    이름:
                    <input 
                        type="text" 
                        name="name" 
                        value={formData.name} 
                        onChange={handleChange} 
                        placeholder="이름을 입력하세요 (최대 50자)"
                        maxLength={50}
                        required 
                    />
                </label>

                <label>
                    부서:
                    <select name="depId" value={formData.depId} onChange={handleDepartmentChange} required>
                        <option value="" disabled>-- 부서 선택 --</option>
                        {departments.map(dept => (
                            <option key={dept.id} value={dept.id}>
                                {dept.name} ({dept.id})
                            </option>
                        ))}
                    </select>
                </label>

                <label>
                    전화번호:
                    <input 
                        type="text" 
                        name="phoneNum" 
                        value={formData.phoneNum} 
                        onChange={handlePhoneChange} 
                        placeholder="010-1234-5678"
                        required 
                    />
                </label>

                <button type="submit" className="submit-button">등록</button>
            </form>
        </div>
    );
}

export default EmployeeCreateView;