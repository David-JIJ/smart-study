package com.example.smart_study.mapper;

import com.example.smart_study.entity.Note;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NoteMapper {

    // 插入笔记：自动获取生成的主键 ID
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO notes(user_id, file_name, file_path) VALUES(#{userId}, #{fileName}, #{filePath})")
    void insert(Note note);

    // 查询用户所有笔记：按上传时间倒序
    @Select("SELECT id, user_id as userId, file_name as fileName, file_path as filePath FROM notes WHERE user_id = #{userId} ORDER BY upload_time DESC")
    List<Note> findByUserId(Integer userId);

    // 根据笔记 ID 获取详细信息
    @Select("SELECT id, user_id as userId, file_name as fileName, file_path as filePath FROM notes WHERE id = #{id}")
    Note getById(Integer id);

    // 🌟 新增：根据 ID 删除笔记
    @Delete("DELETE FROM notes WHERE id = #{id}")
    void deleteById(Integer id);
}
