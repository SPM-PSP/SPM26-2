package com.itheim.program_platform_backend.mapper;

import com.itheim.program_platform_backend.domain.po.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM category WHERE name = #{name}")
    Category selectCategoryByName(@Param("name") String name);


    List<Category> selectCategoriesByNames(@Param("names") List<String> names);
}
