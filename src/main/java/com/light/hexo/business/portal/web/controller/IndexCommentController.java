package com.light.hexo.business.portal.web.controller;

import com.github.pagehelper.PageInfo;
import com.light.hexo.business.admin.model.PostComment;
import com.light.hexo.business.admin.model.Theme;
import com.light.hexo.business.portal.common.CommonController;
import com.light.hexo.business.portal.component.RequestLimit;
import com.light.hexo.common.exception.GlobalException;
import com.light.hexo.common.model.PostCommentRequest;
import com.light.hexo.common.model.Result;
import com.light.hexo.common.util.BrowserUtil;
import com.light.hexo.common.util.IpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author MoonlightL
 * @ClassName: IndexCommentController
 * @ProjectName hexo-boot
 * @Description: 评论控制器（首页）
 * @DateTime 2020/9/21 16:22
 */
@Controller
public class IndexCommentController extends CommonController {

    /**
     * 文章评论列表
     * @param postId
     * @param pageNum
     * @return
     * @throws GlobalException
     */
    @GetMapping("commentList.json")
    @ResponseBody
    public Result commentList(@RequestParam(defaultValue = "0") Integer postId, @RequestParam(defaultValue = "1") Integer pageNum) throws GlobalException {

        Theme activeTheme = this.themeService.getActiveTheme(true);
        String commentShowType = activeTheme.getConfigMap().get("commentShowType");
        List<PostComment> commentList;
        Map<String, Object> map = new HashMap<>();
        if ("singleRow".equals(commentShowType)) {
            // 单行
            commentList = this.postCommentService.listCommentByPostId(postId, pageNum, PAGE_SIZE);
        } else {
            // 多行（父子级评论一起展示）
            commentList = this.postCommentService.getCommentListByPostId(postId, pageNum, PAGE_SIZE);
        }

        PageInfo<PostComment> pageInfo = new PageInfo<>(commentList);
        map.put("totalNum", "singleRow".equals(commentShowType) ? pageInfo.getTotal() : this.postCommentService.getPostCommentNum(postId));
        map.put("commentList", pageInfo.getList());
        map.put("commentShowType", commentShowType);
        return Result.success(map);
    }

    /**
     * 文章评论
     * @param request
     * @param httpServletRequest
     * @return
     * @throws GlobalException
     */
    @PostMapping("auth/sendComment.json")
    @ResponseBody
    @RequestLimit(cacheName = "commentCache", time = 30, msg = "评论操作过于频繁，请等待30秒后再评论")
    public Result sendComment(@Validated(PostCommentRequest.Send.class) PostCommentRequest request, HttpServletRequest httpServletRequest) throws GlobalException {

        PostComment postComment = request.toDoModel();
        String ipAddr = IpUtil.getIpAddr(httpServletRequest);
        postComment.setIpAddress(ipAddr);
        postComment.setBrowser(BrowserUtil.getBrowserName(httpServletRequest));
        this.postCommentService.saveCommentByIndex(postComment);
        return Result.success();
    }

}
