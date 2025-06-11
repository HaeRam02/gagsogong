import React from 'react';
import { useNavigate } from 'react-router-dom';
import './TaskMain.css';

import TaskSearchView from './TaskSearchView';

function TaskMain() {
  const navigate = useNavigate();

  const goToCreate = () => {
    navigate('/task/create');
  };


  return (
    <div style={{ padding: '20px' }}>
      <h1 style={{marginLeft:"20px"}}>업무방</h1>
      
      <h2 style={{marginLeft:"20px"}} onClick={goToCreate}>추진 업무 등록 하기</h2>
      <button 
        onClick={goToCreate} 
        className='addBtn'
      >
        추진 업무 등록
      </button>
      <TaskSearchView />
    </div>
  );
}

export default TaskMain;