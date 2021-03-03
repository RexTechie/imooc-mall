package com.imooc.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.model.dao.CategoryMapper;
import com.imooc.mall.model.pojo.Category;
import com.imooc.mall.model.requests.AddCategoryReq;
import com.imooc.mall.model.vo.CategoryVO;
import com.imooc.mall.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 目录分类实现类
 * @author Rex
 * @create 2021-02-09 12:30
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void add(AddCategoryReq addCategoryReq){
        Category category = new Category();
        BeanUtils.copyProperties(addCategoryReq, category);
        Category categoryOld = categoryMapper.selectByName(category.getName());
        if (categoryOld != null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NAME_EXISTED);
        }
        int count = categoryMapper.insertSelective(category);
        if (count == 0){
            throw new ImoocMallException(ImoocMallExceptionEnum.CREATE_FAILED);
        }
    }

    @Override
    public void update(Category updateCateogry){
        if (updateCateogry.getName() != null){
            Category categoryOld = categoryMapper.selectByName(updateCateogry.getName());
            if (categoryOld != null && !categoryOld.getId().equals(updateCateogry.getId())){
                throw new ImoocMallException(ImoocMallExceptionEnum.NAME_EXISTED);
            }
        }
        int count = categoryMapper.updateByPrimaryKeySelective(updateCateogry);
        if (count == 0){
            throw new ImoocMallException(ImoocMallExceptionEnum.UPDATE_FAILED);
        }
    }

    @Override
    public void delete(Integer id){
        Category categoryOld = categoryMapper.selectByPrimaryKey(id);
        //查不到记录，无法删除，删除失败
        if (categoryOld == null) {
            throw new ImoocMallException(ImoocMallExceptionEnum.DELETE_FAILED);
        }
        int count = categoryMapper.deleteByPrimaryKey(id);
        if (count == 0){
            throw new ImoocMallException(ImoocMallExceptionEnum.DELETE_FAILED);
        }
    }

    @Override
    public PageInfo<Category> listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize, "type, order_num");
        List<Category> categories = categoryMapper.selectList();
        PageInfo<Category> pageInfo = new PageInfo<>(categories);
        return pageInfo;
    }



    @Override
    @Cacheable(value = "listCategoryForCustomer")
    public List<CategoryVO> listCategoryForCustomer(Integer parentId){
        List<CategoryVO> categoryVOList = new ArrayList<>();
        recursivelyFindCategories(categoryVOList, parentId);
        return categoryVOList;
    }

    private void recursivelyFindCategories(List<CategoryVO> categoryVOList, Integer parentId){
        //递归获取所有子类别，并组合成为一个"目录树"
        List<Category> categoryList = categoryMapper.selectCategoriesByParentId(parentId);
        if (!CollectionUtils.isEmpty(categoryList)){
            for (int i = 0; i < categoryList.size(); i++) {
                Category category = categoryList.get(i);
                CategoryVO categoryVO = new CategoryVO();
                BeanUtils.copyProperties(category, categoryVO);
                categoryVOList.add(categoryVO);
                recursivelyFindCategories(categoryVO.getChildCategory(), categoryVO.getId());
            }
        }
    }
}
