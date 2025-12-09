package catchthepattern.com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import catchthepattern.com.afilters.AfilterService;
import catchthepattern.com.afilters.AfilterSet;
import catchthepattern.com.afilters.TickerAfilterSet;
import catchthepattern.com.models.Pattern;
import catchthepattern.com.models.TickerFound;
import catchthepattern.com.services.PatternService;
import catchthepattern.com.services.SearchPatternService;

@CrossOrigin
@RestController
public class AfilterSetController {
    
    @Autowired
    private AfilterService afilterService;

    @PostMapping("/users/afilterSet/save")
    @ResponseBody
    ResponseEntity<String> saveAfilterSet(@RequestBody AfilterSet afilterSet) {
        try {
            afilterService.saveAfilterSet(afilterSet);
            return ResponseEntity.status(HttpStatus.OK).body("Success");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Filterset Saving Error");
        }        
    }
    
    @PostMapping("/users/afilterSet/saveAs")
    @ResponseBody
    ResponseEntity<String> saveAfilterSetAs(@RequestBody AfilterSet afilterSet) {
        try {
            afilterService.saveAfilterSetAs(afilterSet);
            return ResponseEntity.status(HttpStatus.OK).body("Success");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Filterset Saving Error");
        }
        
    }

    @GetMapping("/users/afilterSets")
    @ResponseBody
    List<AfilterSet> getAfilterSets() {
        return afilterService.getUserAfilterSets();
    }

    @GetMapping("/open/allAfilterSets")
    @ResponseBody
    List<AfilterSet> getAllAfilterSets() {
        return afilterService.getAllAfilterSets();
    }

    @GetMapping("/users/afilterSet/{id}")
    @ResponseBody
    Optional<AfilterSet> getAfilterSetById(@PathVariable long id) {
        return afilterService.getAfilterSetById(id);
    }

    @DeleteMapping("/users/afilterSet/delete/{id}")
    public ResponseEntity<String> deleteAfilterSet(@PathVariable int id) {
        try {
            afilterService.deleteAfilterSetById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Success");
        } catch(Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Filterset Deleting Error");
        }
    }
    
    @PostMapping("/users/tickerAfilterSet/save")
    @ResponseBody
    ResponseEntity<String> saveTickerAfilterSet(@RequestBody TickerAfilterSet tickerAfilterSet) {
        try {
            afilterService.saveTickerAfilterSet(tickerAfilterSet);
            return ResponseEntity.status(HttpStatus.OK).body("Success");
        } catch(Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Filterset Ticker Save Error");
        }
    }

    @GetMapping("/open/allTickerAfilterSets")
    @ResponseBody
    List<TickerAfilterSet> getAllTickerAfilterSets() {
        return afilterService.getAllTickerAfilterSets(false);
    }
    
    @GetMapping("/user/tickerAfilterSets")
    @ResponseBody
    List<TickerAfilterSet> getUserTickerAfilterSets() {
        return afilterService.getUserTickerAfilterSets();
    }  

}
