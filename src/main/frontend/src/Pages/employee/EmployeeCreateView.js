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
        depId: '', // depName 필드 제거됨
        phoneNum: ''
    });

    const [departments, setDepartments] = useState([]);

    useEffect(() => {
        const fetchDepartments = async () => {
            try {
                const response = await axios.get('/api/employees/departments');
                const fetchedDepartments = response.data.map(dept => ({
                    id: dept.deptId,
                    name: dept.deptTitle
                }));
                setDepartments(fetchedDepartments);

                if (fetchedDepartments.length > 0) {
                    setFormData(prevData => ({
                        ...prevData,
                        depId: fetchedDepartments[0].id,
                    }));
                }
            } catch (error) {
                console.error("부서 목록 조회 실패:", error);
                alert("부서 목록을 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.");
            }
        };

        fetchDepartments();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleDepartmentChange = (e) => {
        const selectedDeptId = e.target.value;
        setFormData({
            ...formData,
            depId: selectedDeptId,
        });
    };

    const handlePhoneChange = (e) => {
        let value = e.target.value.replace(/[^0-9]/g, '');

        if (value.length <= 3) {
            value = value;
        } else if (value.length <= 7) {
            value = value.replace(/(\d{3})(\d{0,4})/, '$1-$2');
        } else {
            value = value.replace(/(\d{3})(\d{4})(\d{0,4})/, '$1-$2-$3');
        }

        if (value.length > 13) {
            value = value.substring(0, 13);
        }

        setFormData({ ...formData, phoneNum: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.post("/api/employees", formData);
            alert("직원 등록 성공!");
            nav('/employee');
        } catch (error) {
            console.error('직원 등록 실패:', error);
            if (error.response && error.response.data && error.response.data.errors) {
                const errorMessages = error.response.data.errors.map(err => err.message).join('\n');
                alert("등록 실패:\n" + errorMessages);
            } else {
                alert("등록 실패: " + (error.response?.data || "오류 발생"));
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