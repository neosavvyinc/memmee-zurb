package com.memmee.theme.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import com.memmee.attachment.dto.AttachmentMapper;
import com.memmee.theme.dto.Theme;
import com.memmee.theme.dto.ThemeMapper;

public interface ThemeDAO {
	

    @SqlQuery("select * from theme where memmeeId = :memmeeId")
    @Mapper(ThemeMapper.class)
    Theme getTheme(@Bind("memmeeId") Long memmeeId);

    @SqlUpdate("insert into theme (id, memmeeId, name, stylePath) values (:id, :memmeeId, :name, :stylePath)")
    void insert(
         @Bind("id") Long id
        ,@Bind("memmeeId") Long memmeeId
        ,@Bind("name") String name
        ,@Bind("stylePath") String stylePath
    );
    
    @SqlUpdate("update theme set stylePath = :stylePath, name = :name where id = :id")
    int update(
        @Bind("id") Long id
        ,@Bind("stylePath") String stylePath
        ,@Bind("name") String name
    );

    @SqlUpdate("delete from theme where id = :id")
    void delete(
        @Bind("id") Long id
    );
    
    void close();
	

}
