
my $totalscore = 0;
while (<>) {
	chop();
	my ($left,$right) = ($_ =~ m/\D*(\d)\w*(\d)\D*/);
	if (!length($right)) {
		( $right ) = ($_ =~ m/\D*(\d)\D*/);
		$left = $right;
	}
	$totalscore += $left.$right;
}

print "\n== $totalscore\n";
