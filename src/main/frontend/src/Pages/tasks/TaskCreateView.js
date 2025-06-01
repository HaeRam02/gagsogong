import axios from "axios";
import React from "react";
import { useState, useEffect } from "react";
const TaskCreateView = () => {
    const [task, setTask] = useState("");

    useEffect(() => {
        axios.get('/api/tasks').then(res => {
            console.log(res.data)
            setTask(res.data);
        }).catch(err => {
            console.log("조회 실패",err);
        })
    },[])

    return(
  <div>
      <h2>일정 목록</h2>
      <p>{task ? task : "로딩중..."}</p>
    </div>
    );
}

export default TaskCreateView