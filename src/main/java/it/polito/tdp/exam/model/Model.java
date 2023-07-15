package it.polito.tdp.exam.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.exam.db.BaseballDAO;
import it.polito.tdp.exam.model.Event.EventType;

public class Model {
	
	BaseballDAO dao;
	SimpleWeightedGraph<YearPlayers,DefaultWeightedEdge> graph;
	
	public Model() {
		dao = new BaseballDAO();
	}
	
	public List<String> readTeams() {
		return dao.readTeams();
	}
	
	public SimpleWeightedGraph<YearPlayers,DefaultWeightedEdge> creaGrafo(String teamName){
		
		graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		List<YearPlayers> vertex = new ArrayList<>(dao.readAllYears(teamName));
		
		Graphs.addAllVertices(graph, vertex);
		
		for (YearPlayers yp1 : graph.vertexSet())
			for (YearPlayers yp2 : graph.vertexSet())
				if (!yp1.equals(yp2) && (!graph.containsEdge(yp2, yp1))) {
					Set<String> intersezione = new HashSet<>(yp1.getPlayers());
					intersezione.retainAll(yp2.getPlayers());
					Graphs.addEdge(graph, yp1, yp2, intersezione.size());
				}
		
		return graph;
		
	}
	
	public List<Archi> getDettagli(int anno){
		
		List<Archi> result = new ArrayList<>();
		
		YearPlayers vertice = null;
		
		for (YearPlayers yp : graph.vertexSet())
			if (yp.getYear() == anno)
				vertice = yp;
		
		for (YearPlayers yp : Graphs.neighborListOf(graph, vertice))
			result.add(new Archi(yp,(int) graph.getEdgeWeight(graph.getEdge(yp, vertice))));
		
		Collections.sort(result);
		
		return result;
		
	}
	
	private Map<String,Integer> giocatoriTifosi;
	
	public Map<String, Integer> getGiocatoriTifosi() {
		return giocatoriTifosi;
	}

	public void setGiocatoriTifosi(Map<String, Integer> giocatoriTifosi) {
		this.giocatoriTifosi = giocatoriTifosi;
	}

	public int run(int anno, int tifosiTot) {
		
		PriorityQueue<Event> queue = new PriorityQueue<>();
		
		Set<String> players = new HashSet<>();
		
		for (YearPlayers yp : graph.vertexSet())
			players.addAll(yp.getPlayers());
		
		int totPlayers = players.size();
		
		int distr = (int) tifosiTot/totPlayers; 
		
		giocatoriTifosi = new HashMap<>();
		
		
		for (String player : players) {
			queue.add(new Event(player,distr,EventType.RIMANE,anno));
			giocatoriTifosi.put(player, distr);
		}
		
		if (distr * totPlayers < tifosiTot) {
			queue.peek().setNumTifosi(queue.peek().getNumTifosi()+1);
			giocatoriTifosi.put(queue.peek().getPlayerId(),queue.peek().getNumTifosi()+ 1);
		}
		
		List<Integer> anni = new ArrayList<>();
		
		for (YearPlayers yp : graph.vertexSet())
			if (yp.getYear() > anno)
				anni.add(yp.getYear());
		
		Collections.sort(anni);
		
		int ultimoAnno = anni.get(anni.size()-1);
		
		int numTifosiPersi = 0;
		
		
		while (!queue.isEmpty()) {
			
			Event event = queue.poll();
			int year = event.getAnno();
			
			if (year >= ultimoAnno)
				return numTifosiPersi;
			
			System.out.println(event.toString() + '\n');
			
			EventType type = event.getType();
			int numTifosi = event.getNumTifosi();
			String player = event.getPlayerId();
			
			switch (type) {
			
			case RIMANE:
				int rimanenti = (int) (giocatoriTifosi.get(player) - (int) giocatoriTifosi.get(player)*0.9);
				giocatoriTifosi.put(player, (int) (giocatoriTifosi.get(player)*0.9));
				
				Set<String> playersAnnoSucc = new HashSet<>();
				for (YearPlayers yp : graph.vertexSet())
					if (yp.getYear() == year+1)
						playersAnnoSucc = new HashSet<>(yp.getPlayers());
				
				playersAnnoSucc.remove(player);
				List<String> giocatori = new ArrayList<>(playersAnnoSucc);
				String scelto = giocatori.get((int) (Math.random()*(giocatori.size()-1)));
				
				giocatoriTifosi.put(scelto, giocatoriTifosi.get(scelto) + rimanenti);
				
				for (YearPlayers yp : graph.vertexSet())
					if (yp.getYear() == year+1) {
						if (yp.getPlayers().contains(player))
							queue.add(new Event(player,giocatoriTifosi.get(player), EventType.RIMANE, year+1));
						else
							queue.add(new Event(player,giocatoriTifosi.get(player), EventType.SMETTE, year+1));
					}
				for (YearPlayers yp : graph.vertexSet())
					if (yp.getYear() == year+1) {
						if (yp.getPlayers().contains(scelto))
							queue.add(new Event(scelto,giocatoriTifosi.get(scelto), EventType.RIMANE, year+1));
						else
							queue.add(new Event(scelto,giocatoriTifosi.get(scelto), EventType.SMETTE, year+1));
					}
				break;
				
				
			case SMETTE:
				
				int smettono = (int) (0.1*giocatoriTifosi.get(player));
				numTifosiPersi += smettono;
				
				int rimanenti2 = giocatoriTifosi.get(player) - smettono;
				
				giocatoriTifosi.put(player, 0);
				
				List<String> vecchi = new ArrayList<>();
				List<String> nuovi = new ArrayList<>();
				
				for (YearPlayers yp : graph.vertexSet())
					if (yp.getYear() == year-1)
							vecchi = new ArrayList<>(yp.getPlayers());
				
				for (YearPlayers yp : graph.vertexSet())
					if (yp.getYear() == year)
						nuovi = new ArrayList<>(yp.getPlayers());
				
				nuovi.removeAll(vecchi);
				
				String scelto2;
				
				if (Math.random() > 0.25) {
					scelto2 = nuovi.get((int) (Math.random()*(nuovi.size()-1)));
					giocatoriTifosi.put(scelto2, rimanenti2);
				}
				else {
					scelto2 = vecchi.get((int) (Math.random()*(vecchi.size()-1)));
					giocatoriTifosi.put(scelto2, giocatoriTifosi.get(scelto2) + rimanenti2);
				}
				

				for (YearPlayers yp : graph.vertexSet())
					if (yp.getYear() == year+1) {
						if (yp.getPlayers().contains(player))
							queue.add(new Event(scelto2,giocatoriTifosi.get(player), EventType.RIMANE, year+1));
						else
							queue.add(new Event(scelto2,giocatoriTifosi.get(player), EventType.SMETTE, year+1));
					}				
				
				
				break;
				
			}
			
		}
		return numTifosiPersi;
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
