import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './EmployeeSearchView.css';

function EmployeeSearchView() {
    const [employees, setEmployees] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [searchType, setSearchType] = useState('name'); // 검색 조건 상태 추가

    // 검색 조건 옵션들
    const searchOptions = [
        { value: 'name', label: '이름' },
        { value: 'employeeId', label: '직원 ID' },
        { value: 'phoneNum', label: '전화번호' },
        { value: 'depName', label: '부서명' }
    ];

    useEffect(() => {
        fetchAllEmployees();
    }, []);

    const fetchAllEmployees = async () => {
        try {
            const response = await axios.get('/api/employees');
            setEmployees(response.data);
        } catch (error) {
            console.error('직원 목록 조회 실패:', error);
            alert(' 직원 목록 조회에 실패했습니다.');
        }
    };

    const handleSearch = async () => {
        try {
            if (searchTerm.trim() === '') {
                fetchAllEmployees();
                return;
            }

            // 디버깅: 검색 조건과 검색어 출력
            console.log('🔍 검색 시작:', { searchType, searchTerm: searchTerm.trim() });

            // 검색 조건에 따라 기존 EmployeeController API 호출
            let response;
            let apiUrl;

            switch (searchType) {
                case 'name':
                    // 이름으로 검색
                    apiUrl = `/api/employees/name/${encodeURIComponent(searchTerm.trim())}`;
                    console.log(' API 호출:', apiUrl);
                    response = await axios.get(apiUrl);
                    break;

                case 'employeeId':
                    // 직원 ID로 검색
                    apiUrl = `/api/employees/${encodeURIComponent(searchTerm.trim())}`;
                    console.log(' API 호출:', apiUrl);
                    response = await axios.get(apiUrl);
                    // 단일 직원 조회의 경우 배열로 변환
                    if (response.data && !Array.isArray(response.data)) {
                        response.data = [response.data];
                    }
                    break;

                case 'phoneNum':
                    // 전화번호로 검색
                    apiUrl = `/api/employees/phone/${encodeURIComponent(searchTerm.trim())}`;
                    console.log(' API 호출:', apiUrl);
                    response = await axios.get(apiUrl);
                    // 단일 직원 조회의 경우 배열로 변환
                    if (response.data && !Array.isArray(response.data)) {
                        response.data = [response.data];
                    }
                    break;

                case 'depName':
                    // 부서명으로 검색
                    apiUrl = `/api/employees/department/name/${encodeURIComponent(searchTerm.trim())}`;
                    console.log(' API 호출:', apiUrl);
                    response = await axios.get(apiUrl);
                    break;

                default:
                    // 기본값: 전체 조회
                    apiUrl = '/api/employees';
                    console.log(' API 호출:', apiUrl);
                    response = await axios.get(apiUrl);
            }

            console.log(' API 응답:', response.data);
            setEmployees(response.data || []);

        } catch (error) {
            console.error(' 검색 실패:', error);
            console.error(' 에러 상세:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data,
                url: error.config?.url
            });

            if (error.response?.status === 404) {
                setEmployees([]);
                alert(' 검색 결과가 없습니다.');
            } else {
                alert(' 검색에 실패했습니다: ' + (error.response?.data?.message || '오류 발생'));
            }
        }
    };

    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
    };

    const handleSearchTypeChange = (event) => {
        setSearchType(event.target.value);
        setSearchTerm(''); // 검색 조건 변경 시 입력값 초기화
    };

    const handleDelete = async (employeeId) => {
        if (window.confirm('정말로 이 직원을 삭제하시겠습니까?')) {
            try {
                await axios.delete(`/api/employees/${employeeId}`);
                alert(' 직원이 삭제되었습니다.');
                fetchAllEmployees(); // 목록 새로고침
            } catch (error) {
                console.error('삭제 실패:', error);
                alert(' 삭제 실패: ' + (error.response?.data?.message || '오류 발생'));
            }
        }
    };

    // 검색 조건에 따른 placeholder 텍스트 설정
    const getPlaceholder = () => {
        switch (searchType) {
            case 'name':
                return '직원 이름을 입력하세요 (예: 김철수)';
            case 'employeeId':
                return '직원 ID를 입력하세요 (예: emp001)';
            case 'phoneNum':
                return '전화번호를 입력하세요 (예: 010-1234-5678)';
            case 'depName':
                return '부서명을 입력하세요 (예: IT부서)';
            default:
                return '검색어를 입력하세요';
        }
    };

    return (
        <div className="employee-search-container">
            <h1 className="employee-search-header">직원 조회</h1>

            <div className="employee-search-bar">
                {/* 검색 조건 선택 드롭다운 추가 */}
                <div className="search-type-container">
                    <label htmlFor="searchType">검색 조건:</label>
                    <select
                        id="searchType"
                        value={searchType}
                        onChange={handleSearchTypeChange}
                    >
                        {searchOptions.map(option => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </div>

                <div className="search-input-container">
                    <label htmlFor="searchInput">검색어:</label>
                    <input
                        id="searchInput"
                        type="text"
                        className="employee-search-input"
                        placeholder={getPlaceholder()}
                        value={searchTerm}
                        onChange={handleSearchChange}
                        onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    />
                </div>

                <div className="search-buttons-container">
                    <button className="employee-search-button search-btn" onClick={handleSearch}>
                        검색
                    </button>
                    <button className="employee-search-button reset-btn" onClick={fetchAllEmployees}>
                        전체보기
                    </button>
                </div>
            </div>

            {/* 검색 결과 요약 표시 */}
            <div className="search-result-summary">
                <p>
                    <strong>검색 결과:</strong> 총 {employees.length}명의 직원이 조회되었습니다.
                    {searchTerm && (
                        <span className="search-info">
                            {' '}({searchOptions.find(opt => opt.value === searchType)?.label}: "{searchTerm}")
                        </span>
                    )}
                </p>
            </div>

            <table className="employee-search-table">
                <thead>
                <tr>
                    <th>번호</th>
                    <th>직원 ID</th>
                    <th>이름</th>
                    <th>부서명</th>
                    <th>전화번호</th>
                </tr>
                </thead>
                <tbody>
                {employees.length > 0 ? (
                    employees.map((employee, index) => (
                        <tr key={employee.employeeId}>
                            <td>{index + 1}</td>
                            <td>{employee.employeeId}</td>
                            <td>{employee.name}</td>
                            <td>{employee.depName}</td>
                            <td>{employee.phoneNum}</td>
                          


                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="6" className="no-data">
                            {searchTerm ?
                                `"${searchTerm}"에 대한 검색 결과가 없습니다.` :
                                '조회된 직원이 없습니다.'
                            }
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

export default EmployeeSearchView;