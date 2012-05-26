package com.memmee.attachment.dao;

import com.memmee.attachment.dto.Attachment;
import com.memmee.attachment.dto.AttachmentMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

public interface AttachmentDAO {

    @SqlQuery("select * from attachment where memmeeId = :memmeeId")
    @Mapper(AttachmentMapper.class)
    Attachment getAttachment(@Bind("memmeeId") Long memmeeId);

    @SqlUpdate("insert into attachment (id, memmeeId, text, mediaUrl, type) values (:id, :memmeeId, :text, :mediaUrl, :type)")
    void insert(
         @Bind("id") Long id
        ,@Bind("memmeeId") Long memmeeId
        ,@Bind("text") String text
        ,@Bind("mediaUrl") String mediaUrl
        ,@Bind("type") String type
    );
    
    @SqlUpdate("update attachment set text = :text, mediaUrl = :mediaUrl, type = :type where id = :id")
    int update(
        @Bind("id") Long id
        ,@Bind("text") String text
        ,@Bind("mediaUrl") String mediaUrl
        ,@Bind("type") String type
    );

    @SqlUpdate("delete from attachment where id = :id")
    void delete(
        @Bind("id") Long id
    );
    
    void close();

}