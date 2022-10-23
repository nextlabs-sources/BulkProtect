package com.nextlabs.bulkprotect.datatype;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nextlabs.bulkprotect.datatype.actionparams.DecryptActionParams;
import com.nextlabs.bulkprotect.datatype.actionparams.EncryptActionParams;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = EncryptActionParams.class, name = "EncryptActionParams"),
  @JsonSubTypes.Type(value = DecryptActionParams.class, name = "DecryptActionParams")
})
public abstract class ActionParams extends JSONType {
	
	public ActionParams() {}
	
}
