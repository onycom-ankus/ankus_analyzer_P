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

/**
 * @author onycompc
 *
 */
public class Error implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	String message;

	String cause;

	public Error(String code, String message, String cause) {
		super();
		this.code = code;
		this.message = message;
		this.cause = cause;
	}

	public Error() {
	}
	
}
