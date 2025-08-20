package io.github.xzy.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.xzy.novel.core.common.resp.RestResp;
import io.github.xzy.novel.core.constant.DatabaseConsts;
import io.github.xzy.novel.dao.entity.NewsContent;
import io.github.xzy.novel.dao.entity.NewsInfo;
import io.github.xzy.novel.dao.mapper.NewsContentMapper;
import io.github.xzy.novel.dao.mapper.NewsInfoMapper;
import io.github.xzy.novel.dto.resp.NewsInfoRespDto;
import io.github.xzy.novel.manager.cache.NewsCacheManager;
import io.github.xzy.novel.service.NewsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 新闻模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/14
 */
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsCacheManager newsCacheManager;

    private final NewsInfoMapper newsInfoMapper;

    private final NewsContentMapper newsContentMapper;

    @Override
    public RestResp<List<NewsInfoRespDto>> listLatestNews() {
        return RestResp.ok(newsCacheManager.listLatestNews());
    }

    @Override
    public RestResp<NewsInfoRespDto> getNews(Long id) {
        NewsInfo newsInfo = newsInfoMapper.selectById(id);
        QueryWrapper<NewsContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.NewsContentTable.COLUMN_NEWS_ID, id)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        NewsContent newsContent = newsContentMapper.selectOne(queryWrapper);
        return RestResp.ok(NewsInfoRespDto.builder()
            .title(newsInfo.getTitle())
            .sourceName(newsInfo.getSourceName())
            .updateTime(newsInfo.getUpdateTime())
            .content(newsContent.getContent())
            .build());
    }
}
