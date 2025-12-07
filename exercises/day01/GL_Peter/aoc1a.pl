
my $totalscore = 0;
my $pos = 50;
while (<>) {
	chop();	
	my ($dir,$val) = ($_ =~ m/(\w)(\d+)/);
	if ($dir eq "L") {
		$pos -= $val;
	} elsif ($dir eq "R") {
		$pos += $val;
	}
	if ($pos % 100 == 0) {
		$totalscore++;
	}
}

print "\n== $totalscore\n";
