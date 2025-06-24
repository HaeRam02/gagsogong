import React, { useContext } from 'react'; // useContext 임포트 추가
import { useNavigate } from 'react-router-dom';
import { UserContext } from '../../Context/UserContext'; // UserContext 경로 확인 및 임포트
import './EmployeeMain.css';

import EmployeeSearchView from './EmployeeSearchView';

function EmployeeMain() {
    const navigate = useNavigate();
    const { loggedInUser } = useContext(UserContext); // UserContext에서 loggedInUser 가져오기

    const goToCreate = () => {
        // ⭐ 관리자 권한 확인 로직 추가 ⭐
        if (!loggedInUser || loggedInUser.role !== 'ADMIN') {
            alert("❌ 관리자만 직원을 등록할 수 있습니다.");
            return; // 페이지 이동 중단
        }
        navigate('/employee/create');
    };

    return (
        <div style={{ padding: '20px' }}>
            <h1 style={{marginLeft:"20px"}}>직원관리</h1>

            <h2 style={{marginLeft:"20px"}} onClick={goToCreate}>새 직원 등록하기</h2>
            <button
                onClick={goToCreate}
                className='addBtn'>
                직원 등록
            </button>
            <EmployeeSearchView />
        </div>
    );
}

export default EmployeeMain;