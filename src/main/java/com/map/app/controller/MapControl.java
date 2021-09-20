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
import com.map.app.containers.UrlTransformer;
import com.map.app.model.UrlContainer;
import com.map.app.model.RoutePath;
import com.map.app.model.TrafficData;
import com.map.app.service.TrafficAndRoutingService;


@Controller
public class MapControl {
	@Autowired
	TrafficAndRoutingService trs;
	
	
	
	@GetMapping(value="/")
	public String read(Model model)
	{
		model.addAttribute("pt",new UrlTransformer());
		model.addAttribute("bbox",trs.getBoundingBox());
		return "index";
	}
    
	@RequestMapping(value="/routing",method=RequestMethod.GET)
	public String load(@ModelAttribute("pt") UrlTransformer pt, BindingResult errors, Model model)
    {
		
    UrlContainer rp=pt.convert();
	RoutePath res=trs.getPath(rp);
	model.addAttribute("route",res);
	model.addAttribute("bbox",res.getBounds());
   	return "index";
	}
	
	@ResponseBody 
	@RequestMapping(value = "/routing",method=RequestMethod.GET,produces="application/json")   
	 public RoutePath fetchJSONResponse(@ModelAttribute("pt") UrlTransformer pt, BindingResult errors, Model model)
	 {
		
		 UrlContainer rp=pt.convert();
		 RoutePath res=trs.getPath(rp);
		 return res;
	 }
	
    @RequestMapping(value = "/traffic", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public TrafficData show()
    {
    return trs.getAll();	
    }
    
   
    
 
}