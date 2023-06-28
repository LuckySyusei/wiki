package com.luckusyusei.wiki.service;

import com.github.pagehelper.PageInfo;
import com.luckusyusei.wiki.Response.PageResp;
import com.luckusyusei.wiki.Response.UserQueryResp;
import com.luckusyusei.wiki.domain.User;
import com.luckusyusei.wiki.domain.UserExample;
import com.luckusyusei.wiki.exception.BusinessException;
import com.luckusyusei.wiki.exception.BusinessExceptionCode;
import com.luckusyusei.wiki.mapper.UserMapper;
import com.luckusyusei.wiki.req.UserQueryReq;
import com.luckusyusei.wiki.req.UserResetPasswordReq;
import com.luckusyusei.wiki.req.UserSaveReq;
import com.luckusyusei.wiki.util.CopyUtil;
import com.luckusyusei.wiki.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;


@Service
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    @Resource
    private UserMapper userMapper;
    @Resource
    private SnowFlake snowFlake;

    public PageResp<UserQueryResp> list(UserQueryReq req) {
        UserExample userexample = new UserExample();
        UserExample.Criteria criteria = userexample.createCriteria();
        if (!ObjectUtils.isEmpty(req.getLoginName())) {
            criteria.andNameLike(req.getLoginName() );
        }

//        PageHelper.startPage(req.getPage(),req.getSize());
        List<User> userlist = userMapper.selectByExample(userexample);

        PageInfo<User> pageInfo = new PageInfo<>(userlist);
        LOG.info("总行数：{}" , pageInfo.getTotal());
        LOG.info("总页数：{}" ,pageInfo.getPages());

//        List<UserResp> respList =new ArrayList<>();
//        for (User user : userlist) {
////            UserResp userResp = new UserResp();
////            BeanUtils.copyProperties(user,userResp);
//
//            UserResp userResp = CopyUtil.copy(user, UserResp.class);
//            respList.add(userResp);
//        }

        List<UserQueryResp> list = CopyUtil.copyList(userlist, UserQueryResp.class);

        PageResp<UserQueryResp> pageResp = new PageResp();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }
    public void save(UserSaveReq req) {
        User user = CopyUtil.copy(req, User.class);
        if (ObjectUtils.isEmpty(req.getId())) {
            User userDB = selectByLoginName(req.getLoginName());
            if (ObjectUtils.isEmpty(userDB)) {
                // 新增
                user.setId(snowFlake.nextId());
                userMapper.insert(user);
            } else {
                // 用户名已存在
                throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);
            }
        } else {
            // 更新
            user.setLoginName(null);
            user.setPassword(null);
            userMapper.updateByPrimaryKeySelective(user);
        }
    }

        public void delete (Long id){
            userMapper.deleteByPrimaryKey(id);
        }

    public User selectByLoginName(String LoginName) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andLoginNameEqualTo(LoginName);
        List<User>  userList = userMapper.selectByExample(userExample);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        } else {
            return userList.get(0);
        }
    }
    /**
     * 修改密码
     */
    public void resetPassword(UserResetPasswordReq req) {
        User user = CopyUtil.copy(req, User.class);
        userMapper.updateByPrimaryKeySelective(user);
    }

}
