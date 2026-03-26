package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.city_noise_system.dto.HandlingRecordDTO;
import com.example.city_noise_system.entity.HandlingRecord;
import com.example.city_noise_system.vo.HandlingRecordVO;
import java.util.List;

public interface HandlingRecordService extends IService<HandlingRecord> {

    boolean addRecord(Long complaintId, Long handlerId, String action, String remark);

    boolean addRecord(HandlingRecordDTO recordDTO);

    List<HandlingRecordVO> getRecordsByComplaintId(Long complaintId);

    List<HandlingRecordVO> getRecordsByHandlerId(Long handlerId, Integer limit);
}