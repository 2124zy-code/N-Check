package com.ncheck.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ncheck.entity.TbDailyNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TbDailyNoteMapper extends BaseMapper<TbDailyNote> {

    TbDailyNote selectByUserAndDate(@Param("userId") Long userId, @Param("reviewDate") String reviewDate);
}
