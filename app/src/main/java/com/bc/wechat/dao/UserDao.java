package com.bc.wechat.dao;

import com.bc.wechat.cons.Constant;
import com.bc.wechat.entity.User;
import com.bc.wechat.utils.CommonUtil;
import com.bc.wechat.utils.PinyinComparator;

import java.util.Collections;
import java.util.List;

/**
 * 用户DAO
 *
 * @author zhou
 */
public class UserDao {

    /**
     * 保存用户
     * 不存在则新建，存在则更新
     * 唯一标识(userId)
     *
     * @param user 用户
     */
    public void saveUser(User user) {
        List<User> checkList = User.find(User.class, "user_id = ?", user.getUserId());
        user.setUserHeader(CommonUtil.setUserHeader(user.getUserNickName()));
        if (null != checkList && checkList.size() > 0) {
            // 好友已存在，更新基本信息
            User existUser = checkList.get(0);
            user.setId(existUser.getId());
            User.save(user);
        } else {
            // 不存在,插入sqlite
            User.save(user);
        }
    }

    /**
     * 通过用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getUserById(String userId) {
        List<User> userList = User.find(User.class, "user_id = ?", userId);
        if (null != userList && userList.size() > 0) {
            return userList.get(0);
        } else {
            return new User();
        }
    }

    /**
     * 获取所有的好友列表
     *
     * @return 所有的好友列表
     */
    public List<User> getAllFriendList() {
        return User.find(User.class, "is_friend = ?", Constant.IS_FRIEND);
    }

    /**
     * 获取所有的星标好友列表
     * 按好友昵称或备注首字母排序并设置特殊header
     *
     * @return 所有的星标好友列表
     */
    public List<User> getAllStarFriendList() {
        List<User> starFriendList = User.findWithQuery(User.class,
                "select * from user where is_friend = ? and is_star_friend = ?",
                Constant.IS_FRIEND, Constant.RELA_IS_STAR_FRIEND);
        Collections.sort(starFriendList, new PinyinComparator() {
        });
        for (User starFriend : starFriendList) {
            starFriend.setUserHeader(Constant.STAR_FRIEND);
        }
        return starFriendList;
    }

    /**
     * 检查是否为好友
     *
     * @param phone 手机号
     * @return true:是 false:否
     */
    public boolean checkIsFriend(String phone) {
        List<User> friendList = User.findWithQuery(User.class,
                "select * from user where is_friend = ? and user_phone = ?", Constant.IS_FRIEND, phone);
        if (null != friendList && friendList.size() > 0) {
            return true;
        }
        return false;
    }
}
