package hu.hegetomi.webunilogistics.controller;

import hu.hegetomi.webunilogistics.dto.MilestoneDelayDto;
import hu.hegetomi.webunilogistics.service.TransportPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.InvalidParameterException;

@RestController
@RequestMapping("/api/transportplans")
public class TransportPlanController {


    @Autowired
    TransportPlanService transportPlanService;

    @PostMapping("/{transportId}/delay")
    public void delay(@PathVariable long transportId, @RequestBody @Valid MilestoneDelayDto milestoneDelayDto) {
        try {
            transportPlanService.delayTransportPlan(transportId, milestoneDelayDto);
        } catch (InvalidParameterException f) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

}
