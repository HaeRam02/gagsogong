import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './TaskSearchView.css';

function TaskSearchView() {
    const [tasks, setTasks] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        fetchAllTasks();
    }, []);

    const fetchAllTasks = async () => {
        try {
            const response = await axios.get('/api/tasks');
            setTasks(response.data);
        } catch (error) {
            console.error('업무 목록 조회 실패:', error);
        }
    };

    const handleSearch = async () => {
        try {
            const response = await axios.get('/api/tasks/search', {
                params: { title: searchTerm },
            });
            setTasks(response.data);
        } catch (error) {
            console.error('검색 실패:', error);
        }
    };

    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        });
    };

    return (
        <div className="task-search-container">
            <h1 className="task-search-header">업무 조회</h1>

            <div className="task-search-bar">
                <label>업무명:</label>
                <input
                    type="text"
                    className="task-search-input"
                    placeholder="업무 제목 입력"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                />
                <button className="task-search-button" onClick={handleSearch}>검색</button>
            </div>

            <table className="task-search-table">
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>업무 제목</th>
                        <th>담당자</th>
                        <th>시작일</th>
                        <th>종료일</th>
                    </tr>
                </thead>
                <tbody>
                    {tasks.map((task, index) => (
                        <tr key={index}>
                            <td>{index + 1}</td>
                            <td>{task.title}</td>
                            <td>{task.managerName}</td>
                            <td>{formatDate(task.startDate)}</td>
                            <td>{formatDate(task.endDate)}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default TaskSearchView;