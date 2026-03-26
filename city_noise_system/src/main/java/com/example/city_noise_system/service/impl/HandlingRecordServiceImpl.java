package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.city_noise_system.dto.HandlingRecordDTO;
import com.example.city_noise_system.entity.HandlingRecord;
import com.example.city_noise_system.entity.User;
import com.example.city_noise_system.mapper.HandlingRecordMapper;
import com.example.city_noise_system.mapper.UserMapper;
import com.example.city_noise_system.service.HandlingRecordService;
import com.example.city_noise_system.vo.HandlingRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HandlingRecordServiceImpl extends ServiceImpl<HandlingRecordMapper, HandlingRecord>
        implements HandlingRecordService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean addRecord(Long complaintId, Long handlerId, String action, String remark) {
        HandlingRecord record = new HandlingRecord();
        record.setComplaintId(complaintId);
        record.setHandlerId(handlerId);
        record.setAction(action);
        record.setRemark(remark);
        return save(record);
    }

    @Override
    public boolean addRecord(HandlingRecordDTO recordDTO) {
        HandlingRecord record = new HandlingRecord();
        BeanUtils.copyProperties(recordDTO, record);
        return save(record);
    }

    @Override
    public List<HandlingRecordVO> getRecordsByComplaintId(Long complaintId) {
        QueryWrapper<HandlingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("complaint_id", complaintId);
        queryWrapper.orderByAsc("create_time");

        List<HandlingRecord> records = list(queryWrapper);

        return records.stream().map(record -> {
            HandlingRecordVO vo = new HandlingRecordVO();
            BeanUtils.copyProperties(record, vo);

            if (record.getHandlerId() != null) {
                User handler = userMapper.selectById(record.getHandlerId());
                if (handler != null) {
                    vo.setHandlerName(handler.getRealName());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<HandlingRecordVO> getRecordsByHandlerId(Long handlerId, Integer limit) {
        QueryWrapper<HandlingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("handler_id", handlerId);
        queryWrapper.orderByDesc("create_time");

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        List<HandlingRecord> records = list(queryWrapper);

        return records.stream().map(record -> {
            HandlingRecordVO vo = new HandlingRecordVO();
            BeanUtils.copyProperties(record, vo);

            User handler = userMapper.selectById(record.getHandlerId());
            if (handler != null) {
                vo.setHandlerName(handler.getRealName());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}