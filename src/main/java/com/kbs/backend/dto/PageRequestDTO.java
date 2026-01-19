package com.kbs.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.net.URLEncoder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {
    @Builder.Default
    @Min(value = 1)
    @Positive
    private int page = 1;
    @Builder.Default
    @Min(value = 10)
    @Max(value = 100)
    @Positive
    private int size = 10;
    private String link;
    private String type;
    private String keyword;

    public String[] getTypes(){
        if(type==null || type.isEmpty()){
            return null;
        }else{
            return type.split("");
        }
    }

    public Pageable getPageable(String ...props){
        return PageRequest.of(this.page-1, size, Sort.by(props).descending());
    }
    public String getLink() {
        if(link == null){
            StringBuilder builder = new StringBuilder();
            builder.append("page="+this.page);
            builder.append("&size="+this.size);
            if(type!=null && type.length()>0){
                builder.append("&type="+this.type);
            }
            if(keyword!=null && keyword.length()>0){
                try{
                    builder.append("&keyword="+ URLEncoder.encode(this.keyword, "UTF-8"));
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            link = builder.toString();
        }
        return link;
    }
}
