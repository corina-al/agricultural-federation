package edu.hei.school.agricultural.controller.dto;

import java.util.List;

public class Collectivity extends CollectivityInformation {

    public String id;
    public String location;
    public CollectivityStructure structure;
    public List<MemberDto> memberDtos;

    @Override
    public String toString() {
        return "Collectivity{" +
                "id='" + id + '\'' +
                ", location='" + location + '\'' +
                ", structure=" + structure +
                ", members=" + memberDtos +
                ", name='" + name + '\'' +
                ", number=" + number +
                '}';
    }
}