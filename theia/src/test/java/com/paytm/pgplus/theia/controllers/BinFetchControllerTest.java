package com.paytm.pgplus.theia.controllers;

import static com.paytm.pgplus.facade.utils.JsonMapper.mapJsonToObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.theia.controller.test.AbstractControllerTest;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RestController;
import java.io.InputStream;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RestController
public class BinFetchControllerTest extends AbstractControllerTest {
    @Autowired
    ApplicationContext applicationContext;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testBinFetchController() throws Exception {
        BinDetailRequest request = fromJson("/native/binFetchRequest.json", BinDetailRequest.class);
        MvcResult mvcresult = mockMvc
                .perform(
                        MockMvcRequestBuilders.post("/bin/v1/fetchBinDetails").accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        BinDetailResponse response = mapJsonToObject(mvcresult.getResponse().getContentAsString(),
                BinDetailResponse.class);
        assertNotNull(response);
    }

    private <T> T fromJson(String name, Class<T> clazz) throws Exception {
        InputStream stream = BinFetchControllerTest.class.getResourceAsStream(name);
        T o = mapJsonToObject(IOUtils.toString(stream), clazz);
        return o;
    }

}