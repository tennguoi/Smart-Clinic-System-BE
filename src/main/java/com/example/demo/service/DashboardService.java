package com.example.demo.service;

import com.example.demo.dto.TopServiceDTO;
import com.example.demo.repository.InvoiceDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository; // Gọi cái repository vừa tạo ở trên

    public List<TopServiceDTO> getTop5Services() {
        // Lấy 5 kết quả đầu tiên (Top 5)
        Pageable topFive = PageRequest.of(0, 5);
        return invoiceDetailRepository.findTopServices(topFive);
    }
}