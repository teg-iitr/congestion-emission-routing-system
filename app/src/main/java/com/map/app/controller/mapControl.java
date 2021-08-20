package com.map.app.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.map.app.dto.RouteInformationDto;
import com.map.app.model.RouteInformation;
import com.map.app.model.routePath;
import com.map.app.model.trafficdat;
import com.map.app.service.TrafficAndRoutingService;

@Controller
public class mapControl {
	@Autowired
	TrafficAndRoutingService trs;
	
	@GetMapping(value="/")
	public String read(Model model)
	{
		model.addAttribute("pt",new RouteInformationDto());
		return "index";
	}
    
	@RequestMapping(value="/routing",method=RequestMethod.GET)
	public String load(@ModelAttribute("pt") RouteInformationDto pt, BindingResult errors, Model model)
    {
		//System.out.println(pt.toString());
		RouteInformation rp=pt.conv();
	routePath res=trs.getPath(rp);
	//System.out.println(res.getNavigationInstruction());
	model.addAttribute("route",res);
   	return "index";
	}
    
    @RequestMapping(value = "/traffic", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public trafficdat show()
    {
    return trs.getAll();	
    }
}
