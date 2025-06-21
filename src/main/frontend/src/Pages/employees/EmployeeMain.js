import React from 'react';
import { useNavigate } from 'react-router-dom';
import './EmployeeMain.css';

import EmployeeSearchView from './EmployeeSearchView';

function EmployeeMain() {
  const navigate = useNavigate();

  const goToCreate = () => {
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