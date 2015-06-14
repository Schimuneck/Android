/*The MIT License (MIT)

Copyright (c) 2012 Thomas Goossens

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package br.com.rnp.measurements.OWAMP;

/**
 *
 * @author jpingy
 * @page https://code.google.com/p/jpingy/
 *
 * Modified by matias on 13/01/15.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UnixOWAMPResult extends OWAMPResult {

	public UnixOWAMPResult(List<String> pingOutput) {
		super(pingOutput);

	}

	private String[] pack;

	private void generatePackageArray(List<String> lines) {
		if (pack == null) {
			String packages = lines.get(lines.size() - 2);
			pack = packages.split(",");
		}
	}

	@Override
	public int matchTransmitted(List<String> lines) {
		generatePackageArray(lines);
		return Integer.parseInt(pack[0].replaceAll("\\D+", ""));

	}

	@Override
	public int matchReceived(List<String> lines) {
		generatePackageArray(lines);
		return Integer.parseInt(pack[1].replaceAll("\\D+", ""));

	}

	@Override
	public int matchTime(List<String> lines) {
		generatePackageArray(lines);
		return Integer.parseInt(pack[3].replaceAll("\\D+", ""));

	}

	private void generateRttArray(List<String> lines) {
		if (rtt == null) {
			// rtt
			String rtts = lines.get(lines.size() - 1);
			String[] rtt_equals = rtts.split("=");
			rtt = rtt_equals[1].split("/");
		}
	}

	private String[] rtt;

	@Override
	public float matchRttMin(List<String> lines) {
		generateRttArray(lines);
		return Float.parseFloat(rtt[0]);

	}

	@Override
	public float matchRttAvg(List<String> lines) {
		generateRttArray(lines);
		return Float.parseFloat(rtt[1]);
	}

	@Override
	public float matchRttMax(List<String> lines) {
		generateRttArray(lines);
		return Float.parseFloat(rtt[2]);
	}

	@Override
	public float matchRttMdev(List<String> lines) {
		generateRttArray(lines);
		return Float.parseFloat(rtt[3].replaceAll("\\D+", ""));
	}

	@Override
	public String matchIP(List<String> lines) {
		String str = lines.toString();
		String pattern = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		m.find();

		return m.toMatchResult().group(0);
	}

	@Override
	public int matchTTL(List<String> lines) {
		String str = lines.toString();
		Pattern pattern = Pattern.compile("ttl=([0-9\\.]+)"); // match
		// ttl=decimal

		Matcher matcher = pattern.matcher(str.toString());

		matcher.find();
		MatchResult result = matcher.toMatchResult();

		return Integer.parseInt(result.group(1).replaceAll("ttl=", ""));
	}

	

	@Override
	protected int parsePayload(List<String> lines) {
		// TODO Auto-generated method stub

		return Integer.parseInt(lines.get(1).split("bytes")[0].trim());
	}

	//@Override
	public List<OWAMPRequest> getRequests() {

		List<OWAMPRequest> requests = new ArrayList<OWAMPRequest>();
		for (String line : getLines()) {
			if (isPingRequest(line)) {
				OWAMPRequest request = createPingRequest(line);
				requests.add(request);
			}
		}
		return requests;

	}

	private OWAMPRequest createPingRequest(String line) {
		String[] split = line.split(" ");
		OWAMPRequest.PingRequestBuilder builder = OWAMPRequest.builder();

		int bytes = Integer.parseInt(split[0]);
		String from = split[3];
        String fromIP;
        int reqnr;
        int ttl;
        float time;
        if(split[4].contains("(") && split[4].contains(")")) {
            fromIP = split[4].replace("(", "").replace(")", "")
                    .replace(":", "");
            reqnr = Integer.parseInt(split[5].split("=")[1]);
            ttl = Integer.parseInt(split[6].split("=")[1]);
            time = Float.parseFloat(split[7].split("=")[1]);
        } else {
            fromIP = from;
            reqnr = Integer.parseInt(split[4].split("=")[1]);
            ttl = Integer.parseInt(split[5].split("=")[1]);
            time = Float.parseFloat(split[6].split("=")[1]);
        }
		builder = builder.bytes(bytes).from(from).fromIP(fromIP).reqNr(reqnr)
				.ttl(ttl).time(time);

		return builder.build();

	}

	private boolean isPingRequest(String line) {
		return line.contains("bytes from");
	}

}
