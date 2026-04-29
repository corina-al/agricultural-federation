package edu.hei.school.agricultural.controller.dto;

import edu.hei.school.agricultural.api.model.Member;

public class CollectivityStructure {

    public Member president;
    public Member vicePresident;
    public Member treasurer;
    public Member secretary;

    @Override
    public String toString() {
        return "CollectivityStructure{" +
                "president=" + president +
                ", vicePresident=" + vicePresident +
                ", treasurer=" + treasurer +
                ", secretary=" + secretary +
                '}';
    }
}