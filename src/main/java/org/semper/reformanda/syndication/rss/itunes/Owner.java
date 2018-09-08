/**
 * Copyright 2015 Joshua Cain
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.semper.reformanda.syndication.rss.itunes;

public class Owner {

	private String name;
	private String email;

	public String getName() {
		return name;
	}

	public Owner setName(String name) {
		this.name = name;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public Owner setEmail(String email) {
		this.email = email;
		return this;
	}
}