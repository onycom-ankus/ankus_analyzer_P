/*
 * Copyright 2018 by ONYCOM,INC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onycom.ankus4paas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author onycompc
 *
 */

public class Response implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * 성공여부. 
     * 해당 요청에 대해 성공 실패를 나타낸다.
     */
    protected boolean success = false;

    public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public Error getError() {
		
		if (error == null) {
            error = new Error();
        }
        return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public Map<String, Object> getMap() {
        if (map == null) {
            map = new HashMap<String, Object>();
        }
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	public List<Object> getList() {
        if (list == null) {
            list = new ArrayList<Object>();
        }
		return list;
	}

	public void setList(List<Object> list) {
		this.list = list;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	/**
     * 응답의 결과가  하나 이상일때 전체레코드 개수
     */
    protected long total;

    /**
     * 한번의 요청에서 리턴할  레코드 개수
     */
    protected long limit;

    /**
     * 요청 하는 데이터의  시작 위치
     */
    protected long start;

    /**
     * 에러 메시지
     */
    protected Error error;

    /**
     * Key Value 형식의 데이터를 전달할 때 사용하는 노드
     */
    protected Map<String, Object> map;

    /**
     * 레코드 목록을 전달할 때 사용하는 리스트
     */
    protected List<Object> list;

    /**
     * 단일 항목을 리텅할때 사용하는 object
     */
    protected Object object;	

}
