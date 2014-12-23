/*
 *   Copyright 2014 Webpagebytes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.webpagebytes.cms.cmsdata;

import java.util.Date;

public class WPBFile {

	@WPBAdminFieldKey
	private Long privkey;

	@WPBAdminFieldStore
	private String name;

	@WPBAdminFieldStore
	private String externalKey;
	
	@WPBAdminFieldStore
	private String blobKey;
	
	@WPBAdminFieldStore
	private Date lastModified;
	
	@WPBAdminFieldStore
	private String contentType;
	
	@WPBAdminFieldStore
	private String adjustedContentType;
	
	@WPBAdminFieldStore
	private Long size;
	
	@WPBAdminFieldStore
	private String fileName;
	
	@WPBAdminFieldStore
	private Long hash;

	@WPBAdminField
	private String publicUrl;

	@WPBAdminField
	private String thumbnailPublicUrl;

	@WPBAdminFieldStore
	private String thumbnailBlobKey;

	@WPBAdminFieldStore
	private Integer directoryFlag;
	
	@WPBAdminFieldStore
	private String ownerExtKey;
	
	public Long getPrivkey() {
		return privkey;
	}

	public void setPrivkey(Long key) {
		this.privkey = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExternalKey() {
		return externalKey;
	}

	public void setExternalKey(String externalKey) {
		this.externalKey = externalKey;
	}

	public String getBlobKey() {
		return blobKey;
	}

	public void setBlobKey(String blobKey) {
		this.blobKey = blobKey;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
/*
	public String getShortType() {
		return shortType;
	}

	public void setShortType(String shortType) {
		this.shortType = shortType;
	}
*/
	public String getAdjustedContentType() {
		return adjustedContentType;
	}

	public void setAdjustedContentType(String adjustedContentType) {
		this.adjustedContentType = adjustedContentType;
	}

	public Long getHash() {
		return hash;
	}

	public void setHash(Long hash) {
		this.hash = hash;
	}

	public String getPublicUrl() {
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}

	public String getThumbnailPublicUrl() {
		return thumbnailPublicUrl;
	}

	public void setThumbnailPublicUrl(String thumbnailPublicUrl) {
		this.thumbnailPublicUrl = thumbnailPublicUrl;
	}

	public String getThumbnailBlobKey() {
		return thumbnailBlobKey;
	}

	public void setThumbnailBlobKey(String thumbnailBlobKey) {
		this.thumbnailBlobKey = thumbnailBlobKey;
	}

    public Integer getDirectoryFlag() {
        return directoryFlag;
    }

    public void setDirectoryFlag(Integer directoryFlag) {
        this.directoryFlag = directoryFlag;
    }

    public String getOwnerExtKey() {
        return ownerExtKey;
    }

    public void setOwnerExtKey(String ownerExtKey) {
        this.ownerExtKey = ownerExtKey;
    }

	

}