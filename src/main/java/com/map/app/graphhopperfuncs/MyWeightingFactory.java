package com.map.app.graphhopperfuncs;
import static com.graphhopper.routing.weighting.TurnCostProvider.NO_TURN_COST_PROVIDER;
import static com.graphhopper.routing.weighting.Weighting.INFINITE_U_TURN_COSTS;
import static com.graphhopper.util.Helper.toLowerCase;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.CurvatureWeighting;
import com.graphhopper.routing.weighting.DefaultTurnCostProvider;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.routing.weighting.ShortFastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.routing.weighting.custom.CustomModelParser;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.routing.weighting.custom.CustomWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;


//custom weighting factory with "greenest" and "balanced" option
public class MyWeightingFactory implements WeightingFactory{
	private final GraphHopperStorage ghStorage;
    private final EncodingManager encodingManager;
    public MyWeightingFactory(GraphHopperStorage ghStorage, EncodingManager encodingManager) {
        this.ghStorage = ghStorage;
        this.encodingManager = encodingManager;
    }
	@Override
	public Weighting createWeighting(Profile profile, PMap requestHints, boolean disableTurnCosts) {
		// Merge profile hints with request hints, the request hints take precedence.
        // Note that so far we do not check if overwriting the profile hints actually works with the preparation
        // for LM/CH. Later we should also limit the number of parameters that can be used to modify the profile.
        // todo: since we are not dealing with block_area here yet we cannot really apply any merging rules
        // for it, see discussion here: https://github.com/graphhopper/graphhopper/pull/1958#discussion_r395462901
        PMap hints = new PMap();
        hints.putAll(profile.getHints());
        hints.putAll(requestHints);

        FlagEncoder encoder = encodingManager.getEncoder(profile.getVehicle());
        TurnCostProvider turnCostProvider;
        if (profile.isTurnCosts() && !disableTurnCosts) {
            if (!encoder.supportsTurnCosts())
                throw new IllegalArgumentException("Encoder " + encoder + " does not support turn costs");
            int uTurnCosts = hints.getInt(Parameters.Routing.U_TURN_COSTS, 60);
            turnCostProvider = new DefaultTurnCostProvider(encoder, ghStorage.getTurnCostStorage(), uTurnCosts);
        } else {
            turnCostProvider = NO_TURN_COST_PROVIDER;
        }

        String weightingStr = toLowerCase(profile.getWeighting());
        if (weightingStr.isEmpty())
            throw new IllegalArgumentException("You have to specify a weighting");

        Weighting weighting = null;
        if (CustomWeighting.NAME.equalsIgnoreCase(weightingStr)) {
            if (!(profile instanceof CustomProfile))
                throw new IllegalArgumentException("custom weighting requires a CustomProfile but was profile=" + profile.getName());
            CustomModel queryCustomModel = requestHints.getObject(CustomModel.KEY, null);
            CustomProfile customProfile = (CustomProfile) profile;
            queryCustomModel = queryCustomModel == null ?
                    customProfile.getCustomModel() : CustomModel.merge(customProfile.getCustomModel(), queryCustomModel);
            weighting = CustomModelParser.createWeighting(encoder, encodingManager, turnCostProvider, queryCustomModel);
        } else if ("shortest".equalsIgnoreCase(weightingStr)) {
            weighting = new ShortestWeighting(encoder, turnCostProvider);
        } else if ("fastest".equalsIgnoreCase(weightingStr)) {
            if (encoder.supports(PriorityWeighting.class))
                weighting = new PriorityWeighting(encoder, hints, turnCostProvider);
            else
                weighting = new FastestWeighting(encoder, hints, turnCostProvider);
        } else if ("curvature".equalsIgnoreCase(weightingStr)) {
            if (encoder.supports(CurvatureWeighting.class))
                weighting = new CurvatureWeighting(encoder, hints, turnCostProvider);

        } else if ("short_fastest".equalsIgnoreCase(weightingStr)) {
            weighting = new ShortFastestWeighting(encoder, hints, turnCostProvider);
        }
        //"balanced"
        else if("balanced".equalsIgnoreCase(weightingStr)){
        	weighting = new BalancedWeighting(encoder, hints, turnCostProvider);
        }
        //"greenest"
        else if("greenest".equalsIgnoreCase(weightingStr))
        {
        	weighting=new GreenestWeighting(encoder,turnCostProvider);
        }

        if (weighting == null)
            throw new IllegalArgumentException("Weighting '" + weightingStr + "' not supported");

        return weighting;
	}

}
