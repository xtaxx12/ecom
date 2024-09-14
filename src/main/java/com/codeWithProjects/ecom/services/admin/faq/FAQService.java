package com.codeWithProjects.ecom.services.admin.faq;

import com.codeWithProjects.ecom.dto.FAQDto;

public interface FAQService {

    public FAQDto postFAQ(long productId, FAQDto faqDto);
}
