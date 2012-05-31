package com.memmee.attachment.dto;

import java.io.Serializable;


public class Attachment implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5360464163625623532L;

	private Long id;

    private Long memmeeId;

    private String filePath;
    
    private String type;
    
  
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemmeeId() {
		return memmeeId;
	}

	public void setMemmeeId(Long memmeeId) {
		this.memmeeId = memmeeId;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


		    

		    
		   

}
