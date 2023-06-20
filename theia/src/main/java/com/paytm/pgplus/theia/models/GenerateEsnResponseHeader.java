package com.paytm.pgplus.theia.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GenerateEsnResponseHeader {
    private static final long serialVersionUID = -6765156369667631851L;

    private Long responseTimestamp;
    private String version;
}